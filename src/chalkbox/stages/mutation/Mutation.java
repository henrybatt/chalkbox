package chalkbox.stages.mutation;

import chalkbox.api.common.java.JUnitIndividualResult;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.*;

import com.google.common.flogger.FluentLogger;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.mutationtest.tooling.MutationCoverage;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
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

    private final int maxScore;

    public Mutation(int maxScore) {
        this.maxScore = maxScore;
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

//        // Run tests against the solution
//        var classPath = solution.getClassPath() +
//                File.pathSeparator + solution.getSrcBuildPath() +
//                File.pathSeparator + solution.getTestBuildPath();
//        var baselineResults = this.runTests(tests, classPath);

        // Path contains dependencies and the compile submission
        var classPath = submission.getClassPath() +
                File.pathSeparator + submission.getSrcBuildPath() +
                File.pathSeparator + submission.getTestBuildPath();
        var submissionResults = this.runTests(tests, classPath); //todo(mh): fail here if they dont pass their own tests?


        var e = new EntryPoint();
        ReportOptions data = new ReportOptions();
        // Set the classes to mutate
        data.setTargetClasses(Collections.singletonList("tms.sensors.DemoPressurePad"));

        // Set the tests to run against the mutations
        var packages = new ArrayList<Predicate<String>>();
        packages.add(new Glob("*"));
        data.setTargetTests(packages);

        // Set classpath elements (compiled code, test code, dependencies)
        // This is where it gets complicated; you need to find the correct paths.
        var path = new ArrayList<String>();
        path.add(submission.getClassPath()); //todo(mh): do we need to split by ":", replace with just libs needed
        path.add(submission.getSrcBuildPath());
        path.add(submission.getTestBuildPath());
        String classpath = System.getProperty("java.class.path");
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
        data.addOutputFormats(Collections.singletonList("HTML"));
        data.setOutputEncoding(StandardCharsets.UTF_8);
        data.setInputEncoding(StandardCharsets.UTF_8);
        data.setVerbosity(Verbosity.VERBOSE);

        PluginServices plugins = PluginServices.makeForContextLoader();
        var result = e.execute(null, data, plugins, new HashMap<>());


        var stats = result.getStatistics().get();


        var overview = new Result(name);
//        overview.setScore(scaled)
//                .setMaxScore(maxScore)
//                .appendOutput(table + equation)
//                .setOutputFormat("md")
//                .setVisibility(Visibility.AFTER_PUBLISH);

        return StageResult.fromOverview(overview);
    }

    @Override
    public StageResult run(Submission submission, List<Solution> solutions) throws StageException {
        // Not implemented
        return null;
    }

    /**
     * Run the tests on a submission.
     * <p>
     * If there were issues compiling the sample solution or the tests, or
     * the submission did not compile successfully, no action is taken.
     * <p>
     * Uses a JUnit listener to observe the passed/failed tests for each test
     * class. One Gradescope test is created for each JUnit test method, with
     * a mark of zero if the test failed, or a mark of
     * <code>stageWeighting / numTests</code> if the test passed, where
     * <code>stageWeighting</code> is the number of marks allocated to this
     * stage, and <code>numTests</code> is the total number of JUnit test
     * methods in all test classes.
     */
    @Override
    public StageResult run(Submission submission, Solution solution) throws StageException {
        // Not implemented
        return null;
    }

    private Map<String, List<JUnitIndividualResult>> runTests(List<String> tests, String classPath) {
        var collection = new HashMap<String, List<JUnitIndividualResult>>();
        for (String className : tests) {
            // Ignore any that dont end in TEST
            if (!className.endsWith("Test")) {
                continue;
            }

            var results = JUnitRunner.runTests(className, classPath);
            if (results.isEmpty()) {
                continue;
            }
            results.sort(Comparator.comparing(JUnitIndividualResult::name));
            collection.put(className, results);
        }
        return collection;
    }
}
