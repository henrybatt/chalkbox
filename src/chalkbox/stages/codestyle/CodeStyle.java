package chalkbox.stages.codestyle;

import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;
import chalkbox.source.Solution;
import chalkbox.stages.*;
import chalkbox.source.Submission;
import com.google.common.flogger.FluentLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Processor to execute the Checkstyle tool on the submission.
 */
public class CodeStyle implements Stage {

    private static final String name = "Code Style";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private String jar;
    private String config;
    private double weighting = 0;
    private float penaltyPerInfraction = 0;
    private final List<String> excludedFiles;

    /**
     * Sets up the Checkstyle stage ready to process a submission.
     */
    public CodeStyle(String jar, String config, double weighting, float penaltyPerInfraction, List<String> excludedFiles) {
        this.jar = jar;
        this.config = config;
        this.weighting = weighting;
        this.penaltyPerInfraction = penaltyPerInfraction;
        this.excludedFiles = excludedFiles;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return Type.SUBMISSION_ONLY;
    }

    public StageResult run(Submission submission) throws StageException {
        var result = new Result(name).setOutputFormat("md");
        result.setMaxScore(weighting);

        try {
            var compilation = submission.compileSrc();
            if (!compilation.success()) {
                result.appendOutput("Unable to compile: " + compilation.output());
                return StageResult.fromOverview(result);
            }
        } catch (IOException e) {
            result.appendOutput("Submission did not compile, not checking automated style");
            result.appendOutput(e.toString());
            return StageResult.fromOverview(result);
        }
        logger.atInfo().log("Filepath for the checkstyle tool: %s with config %s", jar, config);

        var path = Paths.get(config);
        var configPath = path.toAbsolutePath().getParent().toString();

        List<String> processArgs = new ArrayList<>();
        processArgs.add("java");
        processArgs.add(String.format("-Dcheckstyle.config.path=%s", configPath));
        processArgs.add("-jar");
        processArgs.add(jar);
        processArgs.add("-c");
        processArgs.add(config);
        processArgs.addAll(generateExcludedArgs(excludedFiles));
        processArgs.add(submission.getSrcFolder());
        logger.atInfo().log("Running CheckStyle %s", String.join(" ", processArgs));

        // todo(mh): Change this to # * timeunit.Seconds or likewise
        ProcessExecution process = null;
        try {
            process = Execution.runProcess(20000,
                    processArgs.toArray(String[]::new));
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        var checkstyleError = process.getError();
        if (!checkstyleError.isEmpty()) {
            throw new StageException(checkstyleError);
        }

        var checkstyleOutput = process.getOutput();
        if (!checkstyleOutput.contains("Audit done.")) {
            result.appendOutput("""
                    ❌ Checkstyle did not exit successfully. \
                    This can indicate a syntax error or missing files.\
                    
                    ### Details
                    
                    """);
            result.appendOutput(checkstyleOutput);
            return StageResult.fromOverview(result);
        }

        // count violations based on lines in output
        // subtract 2 for header/footer lines
        var violations = Math.max(0, checkstyleOutput.split("\n").length - 2);

        result.setScore((int) Math.max(0, weighting - (violations * penaltyPerInfraction)));

        var formattedOutput = Arrays.stream(checkstyleOutput.split("\n"))
                .filter(n -> !n.contains("Starting audit") && !n.contains("Audit done"))
                .map(n -> n.replace("[WARN] ", "❌ "))
                .map(n -> n.replace(submission.getBasePath(), ""))
                .collect(Collectors.joining("\n"));

        result.appendOutput(String.format("""
                A total of %d style violations.
                
                =============
                %s
                """, violations, formattedOutput));
        return StageResult.fromOverview(result);
    }

    @Override
    public StageResult run(Submission submission, Solution solution) throws StageException {
        return null;
    }

    @Override
    public StageResult run(Submission submission, List<Solution> solutions) throws StageException {
        return null;
    }

    /**
     * Transforms the given list of excluded directories to a list of command
     * line arguments for the Checkstyle tool.
     *
     * @param excluded list of excluded paths
     * @return list of command line arguments specifying excluded paths
     */
    private List<String> generateExcludedArgs(List<String> excluded) {
        List<String> args = new ArrayList<>();
        for (String s : excluded) {
            args.add("-e");
            args.add(s);
        }
        return args;
    }
}
