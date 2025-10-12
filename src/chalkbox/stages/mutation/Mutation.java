package chalkbox.stages.mutation;

import chalkbox.api.common.java.JUnitIndividualResult;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.*;

import com.google.common.flogger.FluentLogger;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.Services;
import org.pitest.mutationtest.config.ServicesFromClassLoader;
import org.pitest.mutationtest.tooling.AnalysisResult;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Verbosity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class Mutation implements Stage {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    public final static String name = "Mutation";

    private final double maxScore;
    private final List<String> mutationTargets;
    private final List<String> testTargets;
    private final List<String> ignoreTests;

    public Mutation(double maxScore, List<String> mutationTargets, List<String> testTargets, List<String> ignoreTests) {
        this.maxScore = maxScore;
        this.mutationTargets = mutationTargets;
        this.testTargets = testTargets;
        this.ignoreTests = ignoreTests;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return Type.SUBMISSION_ONLY;
    }

    @Override
    public StageResult run(Submission submission) throws StageException {
        // Compile the solution, tests and the submission
        try {
            var compilation = submission.compileSrc();
            if (!compilation.success()) {
                throw new StageException("Unable to compile submission: " + compilation.output());
            }
            compilation = submission.compileTest();
            if (!compilation.success()) {
                throw new StageException("Unable to compile submission tests: " + compilation.output());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> tests = null;
        try {
            tests = submission.getTestClasses();
        } catch (IOException e) {
            throw new StageException(e.toString());
        }

        // Path contains dependencies and the compile submission
        var classPath = submission.getClassPath() +
                File.pathSeparator + submission.getSrcBuildPath() +
                File.pathSeparator + submission.getTestBuildPath();
        List<String> failingTests = this.runTests(tests, classPath);
        if (!failingTests.isEmpty()) {
            var overview = new Result(name)
                    .appendOutput("Some of your JUnit tests failed when run against your solution therefore mutation testing was not executed.\n")
                    .appendOutput("## Details:\n\n")
                    .setStatus(Status.PASSED);
            for (String fail : failingTests) {
                overview.appendOutput(fail);
            }
            return StageResult.fromOverview(overview);
        }


        ReportOptions data = new ReportOptions();
        // Set the classes to mutate
        data.setTargetClasses(mutationTargets);

        // Set the tests to run against the mutations
        var packages = new ArrayList<Predicate<String>>();
        // TODO(bw): this is any match deal so we need to be a bit smarter about ignore vs include
        // TODO(bw): for this assignment we only want to ignore and implicitly include all
        //for (var test : testTargets) {
        //    packages.add(new Glob(test));
        //}
        for (var test : ignoreTests) {
            packages.add((path) -> !new Glob(test).matches(path));
        }
        data.setTargetTests(packages);

        // Set classpath elements (compiled code, test code, dependencies)
        // This is where it gets complicated; you need to find the correct paths.
        var path = new ArrayList<String>();
        path.add(submission.getClassPath()); //todo(mh): do we need to split by ":", replace with just libs needed
        path.add(submission.getSrcBuildPath());
        path.add(submission.getTestBuildPath());
        String classpath = System.getProperty("java.class.path") + File.pathSeparator + classPath;
        System.out.println("Full Classpath: " + classpath);

        // Split the classpath into individual entries
        String[] classPathEntries = classpath.split(File.pathSeparator);
        path.addAll(List.of(classPathEntries));
        data.setClassPathElements(path); // Custom method to get paths

        // Set source directories for report generation
        var sourceList = new ArrayList<Path>();
        sourceList.add(Path.of(submission.getSrcFolder()));
        sourceList.add(Path.of(submission.getTestFolder()));
        data.setSourceDirs(sourceList);

        // Set output directory
        data.setReportDir("target/custom-pit-report");

        // Set mutators, threads, etc. (optional, defaults are often fine)
        data.setMutators(Collections.singletonList("DEFAULTS"));

        data.setGroupConfig(new TestGroupConfig());
        data.addOutputFormats(Collections.singletonList("Chalkbox"));
        data.setOutputEncoding(StandardCharsets.UTF_8);
        data.setInputEncoding(StandardCharsets.UTF_8);
        data.setVerbosity(Verbosity.QUIET);

        MutationListener listener = new MutationListener();
        PluginServices plugins = injectListener(listener);
        AnalysisResult result;
        try {
            var e = new EntryPoint();
            result = e.execute(null, data, plugins, new HashMap<>());
        } catch (Exception err) {
            return failWithMessage(err.getMessage());
        }
        if (result.getError().isPresent()) {
            return failWithMessage(result.getError().get().toString());
        }

        var overview = new Result(name)
                .appendOutput("Below are mutations (changes) that have been made to your submission and whether or not your unit tests successfully identified the change.")
                .setStatus(Status.PASSED);

        return new StageResult(overview, listener.getResults());
    }

    private StageResult failWithMessage(String cause) {
        String message;
        if (cause.contains("Mutation testing requires a green")) {
            message = "Unable to run mutation tests while your unit tests do not pass on your solution. ";
            message += "Your JUnit tests must pass when run against your submission.";
        } else {
            message = "Unable to execute mutation tests: " + cause;
        }
        return StageResult.fromOverview(
                new Result(name)
                        .setStatus(Status.FAILED)
                        .setOutputFormat(message)
        );
    }

    private PluginServices injectListener(MutationListener listener) {
        Services fallback = new ServicesFromClassLoader(IsolationUtils.getContextClassLoader());
        Services serviceLoader = new Services() {
            @Override
            @SuppressWarnings("unchecked") // hopefully valid
            public <S> Collection<S> load(Class<S> ifc) {
                // intercept lookup and include custom listener
                if (ifc.isAssignableFrom(MutationResultListenerFactory.class)) {
                    return List.of((S) listener);
                }
                // fallback to default
                return fallback.load(ifc);
            }
        };

        return new PluginServices(serviceLoader);
    }

    @Override
    public StageResult run(Submission submission, List<Solution> solutions) throws StageException {
        // Not implemented
        return null;
    }

    @Override
    public StageResult run(Submission submission, Solution solution) throws StageException {
        // Not implemented
        return null;
    }

    private List<String> runTests(List<String> tests, String classPath) {
        var failures = new ArrayList<String>();
        for (String className : tests) {
            // Ignore any that dont end in TEST
            if (className.contains("scenario")) {
                continue;
            }

            var results = JUnitRunner.runTests(className, classPath);
            if (results.isEmpty()) {
                continue;
            }
            for (var result : results) {
                if (result.fails() > 0) {
                    failures.add("JUnit test: `" + result.name() + "` fails. Output:\n");
                    failures.add("```" + result.output() + "```\n\n");
                }
            }
        }
        return failures;
    }
}
