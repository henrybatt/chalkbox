package chalkbox.java.test;

import chalkbox.api.annotations.Pipe;
import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.java.compilation.JavaCompilation;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

/**
 * Process to execute JUnit tests on each submission.
 *
 * <p>Requires {@link JavaCompilation} process to be executed first.
 * Checks if compilation.compiles is true.
 *
 * <p>An example output is shown below, where ... is the raw test output:
 * <pre>
 * "tests": {
 *     "package1.ClassOneTest": {
 *         "output": "...",
 *         "total": 12,
 *         "passes": 8,
 *         "fails": 4,
 *         "errors": ""
 *     }
 * }
 * </pre>
 *
 * <p>If a test class times out while being executed, the below will be given:
 * <pre>
 * "tests": {
 *     "package1.TimedOutTest": {
 *         "errors": "Timed out"
 *     }
 * }
 * </pre>
 */
public class JavaTest {
    private Bundle tests;
    protected boolean hasErrors;

    /** Sample solution to compile tests with */
    private String solutionPath;

    /** Path of JUnit test files */
    private String testPath;

    /** Class path for tests to be compiled with */
    private String classPath;

    /** Number of marks allocated to the functionality stage */
    private int weighting;

    public JavaTest(String solutionPath, String testPath, String classPath,
            int weighting) {
        this.solutionPath = solutionPath;
        this.testPath = testPath;
        this.classPath = classPath;
        this.weighting = weighting;

        this.compileTests();
    }

    /**
     * Compile the sample solution and then compile the tests with the sample
     * solution.
     */
    public void compileTests() {
        tests = new Bundle(new File(testPath));
        Bundle solution = new Bundle(new File(solutionPath));

        /* Load output directories for the solution and the tests */
        Bundle solutionOutput;
        Bundle testOutput;
        try {
            solutionOutput = new Bundle();
            testOutput = new Bundle();
            /* Add the tests to the class path for execution */
            classPath = classPath + System.getProperty("path.separator") + testOutput.getUnmaskedPath();
        } catch (IOException e) {
            hasErrors = true;
            e.printStackTrace();
            return;
        }

        StringWriter output = new StringWriter();

        /* Compile the sample solution */
        Compiler.compile(Compiler.getSourceFiles(solution), classPath,
                solutionOutput.getUnmaskedPath(), output);

        /* Compile the tests with the sample solution */
        Compiler.compile(Compiler.getSourceFiles(tests),
                classPath + System.getProperty("path.separator") + solutionOutput.getUnmaskedPath(),
                testOutput.getUnmaskedPath(), output);
    }

    /**
     * Run the tests on a submission
     */
    public Collection run(Collection submission) {
        if (hasErrors) {
            return submission;
        }
        if (!submission.getResults().is("extra_data.compilation.compiles")) {
            return submission;
        }

        String classPath = this.classPath + System.getProperty("path.separator") + submission.getWorking().getUnmaskedPath("bin");
        JSONArray testResults = (JSONArray) submission.getResults().get("tests");
        int totalNumTests = 0;
        JSONArray functionalityResults = new JSONArray();
        for (String className : tests.getClasses("")) {
            List<Data> results = JUnitRunner.runTests(className, classPath);
            /* Sort alphabetically by test class then test name */
            results.sort(Comparator.comparing(o -> ((String) o.get("name"))));

            for (Data result : results) {
                totalNumTests++;
                functionalityResults.add(result);
            }
        }
        if (totalNumTests == 0) {
            return submission;
        }

        /* Mark awarded for passing a single test method */
        final double individualTestWeighting = 1d / totalNumTests
                * this.weighting;

        for (Object o : functionalityResults) {
            Data functionalityResult = (Data) o;
            boolean didPass = (Integer) functionalityResult.get("extra_data.passes") == 1;
            functionalityResult.set("score", didPass ? individualTestWeighting : 0);
            functionalityResult.set("max_score", individualTestWeighting);
            testResults.add(functionalityResult);
        }

        return submission;
    }
}
