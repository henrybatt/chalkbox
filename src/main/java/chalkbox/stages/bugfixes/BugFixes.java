package chalkbox.stages.bugfixes;

import chalkbox.api.common.java.JUnitIndividualResult;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.*;
import chalkbox.stages.conformance.SourceLoader;
import chalkbox.stages.functionality.ClassResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Bug fixes differs from functionality in that there is a baseline
 * number of passing tests so the formula is
 *  M = Max(0, (P - Bp)/Bf)
 * where Bp is the number of passing tests in the provided code,
 * Bf is the number of failing tests in the provided code, and
 * P is the number of tests that pass in the submission.
 */
// TODO: This should really share as much of Functionality as possible
public class BugFixes implements Stage {

    public final static String name = "Bug Fixes";

    private final double weighting;
    private final double providedPassing;
    private final double providedFailing;

    public BugFixes(double weighting, double providedPassing, double providedFailing) {
        this.weighting = weighting;
        this.providedPassing = providedPassing;
        this.providedFailing = providedFailing;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return Type.SUBMISSION_AND_SOLUTION;
    }

    @Override
    public StageResult run(Submission submission) throws StageException {
        // Not implemented
        return null;
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
        // Compile the solution, tests and the submission
        try {
            var compilation = solution.compileSrc();
            if (!compilation.success()) {
                throw new StageException("Unable to compile solution: " + compilation.output());
            }
            compilation = solution.compileTest();
            if (!compilation.success()) {
                throw new StageException("Unable to compile tests: " + compilation.output());
            }
            compilation = submission.compileSrc();
            if (!compilation.success()) {
                throw new StageException("Unable to compile submission: " + compilation.output());
            }
        } catch (IOException e) {
            throw new StageException(e);
        }

        List<String> tests = null;
        try {
            tests = solution.getTestClasses();
        } catch (IOException e) {
            throw new StageException(e.toString());
        }

        // Run tests against the solution
        var classPath = solution.getClassPath() +
                File.pathSeparator + solution.getSrcBuildPath() +
                File.pathSeparator + solution.getTestBuildPath();
        var baselineResults = this.runTests(tests, classPath);

        // Path contains dependencies and the compile submission
        classPath = solution.getClassPath() +
                File.pathSeparator + submission.getSrcBuildPath() +
                File.pathSeparator + solution.getTestBuildPath();
        var submissionResults = this.runTests(tests, classPath);

        var totalNumTests = 0;
        var innerResults = new ArrayList<Result>();
        var classResults = new ArrayList<ClassResult>();

        for (String className : tests) {
            if (!className.endsWith("Test")) {
                continue;
            }

            int classPassing = 0;

            // Use test summaries to collect information even if test fails to compile
            var classTests = baselineResults.get(className).size();
            var classWeighting = baselineResults.get(className).getFirst().classWeight();

            for (JUnitIndividualResult unit : submissionResults.get(className)) {
                var isPassing = unit.passes() == 1;
                var visibility = Visibility.VISIBLE;
                var unitResult = new Result("Provided Tests: " + unit.name())
                        .setVisibility(visibility)
                        .setStatus(isPassing ? Status.PASSED : Status.FAILED);

                if (!isPassing) {
                    unitResult.appendOutput("❌ Test scenario fails\n");

                    // Get Test class JavaDoc
                    var testDescription = getTestJavaDoc(solution.getTestBuildPath(), className, unit.name());
                    if (!testDescription.isEmpty()) {
                        unitResult.appendOutput("### Scenario\n");
                        unitResult.appendOutput(testDescription);
                    }

                    unitResult.appendOutput("### Details\n");
                    unitResult.appendOutput(unit.output());
                }

                var testMultiplier = (Integer) unit.weight();
                // e.g. a test worth 5 "units" will increase the total number of tests by 5
                totalNumTests += testMultiplier;
                innerResults.add(unitResult);
                classPassing += unit.passes() == 1 ? 1 : 0;
            }
            classResults.add(new ClassResult(className, classTests, classPassing, classWeighting, submissionResults.get(className).size()));
        }

        if (totalNumTests == 0) {
            // todo(mh): Do something better here
            return null;
        }

        double total = 0;
        double possible = 0;
        for (var classResult : classResults) {
            if (classResult.count() <= 0) {
                continue;
            }
            total += classResult.passing();
            possible += classResult.count();
        }
        double scaled = Math.ceil(100 * Math.max(0, (total - providedPassing) / providedFailing));

        String message = "When provided, " + providedPassing + " tests passed and " + providedFailing + " tests failed.\n";
        message += "Now " + total + " tests pass and " + (possible - total) + " tests fail.";

        var equation = "\n$$\nresult = \\dfrac{" + total + " - " + providedPassing + "}{" + providedFailing + "} = " + scaled + "%\n$$";
        var overview = new Result(name);
        overview.setMaxScore(weighting)
                .setScore(scaled * (weighting/100))
                .appendOutput(message + equation)
                .setOutputFormat("md")
                .setVisibility(Visibility.AFTER_PUBLISHED);

        return new StageResult(overview, innerResults);
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

    private String getTestJavaDoc(String folder, String className, String methodName) {
        try {
            var testDescription = new StringBuilder();
            var javaDoc = new SourceLoader(folder).getTestJavadoc(className);
            for (var method : javaDoc.getMethods()) {
                if (method.getName().equals(methodName.split("\\.")[1])) {
                    testDescription.append(method.getComment()).append("\n");
                }
            }
            return testDescription.toString();
        } catch (IOException ignored) {
            // Do Nothing
        }
        return "";
    }
}
