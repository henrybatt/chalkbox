package chalkbox.stages.codestyle;

import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;
import chalkbox.source.Solution;
import chalkbox.stages.*;
import chalkbox.source.Submission;
import com.google.common.flogger.FluentLogger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Processor to execute the Checkstyle tool on the submission.
 */
public class CodeStyle implements Stage {

    private static final String name = "Code Style";
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private double weighting = 0;
    private float penaltyPerInfraction = 0;
    private final List<String> excludedFiles;
    private String customCheckstyleConfig = "";

    /**
     * Sets up the Checkstyle stage ready to process a submission.
     */
    public CodeStyle(double weighting, float penaltyPerInfraction, List<String> excludedFiles) {
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

    public CodeStyle overrideCheckstyleConfig(String filePath) {
        this.customCheckstyleConfig = filePath;
        return this;
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

        String checkstyleJar = null;
        String checkstyleConfig = null;
        try {
            checkstyleJar = getJarFilePathFromResource("checkstyle.jar");
            checkstyleConfig = getJarFilePathFromResource("checkstyle.xml");
        } catch (Exception e) {
            throw new StageException(e.toString());
        }
        logger.atInfo().log("Filepath for the checkstyle tool: %s with config %s", checkstyleJar, checkstyleConfig);

        List<String> processArgs = new ArrayList<>();
        processArgs.add("java");
        processArgs.add("-jar");
        processArgs.add(checkstyleJar);
        processArgs.add("-c");
        processArgs.add(customCheckstyleConfig.isEmpty() ? checkstyleConfig : customCheckstyleConfig);
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

    private static String getJarFilePathFromResource(String resourcePath) throws URISyntaxException {
        URL resourceUrl = CodeStyle.class.getClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
            return "Resource not found: " + resourcePath;
        }

        String urlString = resourceUrl.toString();
        if (urlString.startsWith("file:")) {
            return new java.io.File(resourceUrl.toURI()).getAbsolutePath();
        }

        if (urlString.startsWith("jar:file:")) {
            // The format is "jar:file:/FULL/PATH/TO/jarName.jar!/PATH/TO/RESOURCE"
            int bangIndex = urlString.indexOf('!');
            if (bangIndex != -1) {
                String jarPathWithPrefix = urlString.substring("jar:".length(), bangIndex);
                String decodedJarPath = URLDecoder.decode(jarPathWithPrefix, StandardCharsets.UTF_8);

                URI uri = new URI(decodedJarPath);
                return uri.getPath();
            }
        }

        throw new StageException("Unable to find resources: " + resourcePath);
    }
}
