package chalkbox.java.functionality;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Configuration;
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
 * Requires {@link JavaCompilation} process to be executed first.
 * Checks if "extra_data.compilation.compiles" is true.
 */
public class Functionality {

    public static class FunctionalityOptions implements Configuration {

        /** Whether or not to run this stage */
        private boolean enabled = false;

        /** Sample solution to compile tests with */
        private String correctSolution;

        /** Class path for tests to be compiled with */
        private String classPath;

        /** Number of marks allocated to the functionality stage */
        private int weighting;

        /** Path of JUnit test files */
        private String testDirectory;

        /**
         * Checks this configuration and throws an exception if it is invalid.
         *
         * @throws ConfigFormatException if the configuration is invalid
         */
        @Override
        public void validateConfig() throws ConfigFormatException {
            if (!enabled) {
                return;
            }

            /* Must have a test directory */
            if (testDirectory == null || testDirectory.isEmpty()) {
                throw new ConfigFormatException(
                        "Missing testDirectory in functionality stage");
            }

            /* Must have a weighting between 0 and 100 */
            if (weighting < 0 || weighting > 100) {
                throw new ConfigFormatException(
                        "Functionality weighting must be between 0 and 100");
            }
        }

        //<editor-fold desc="JavaBeans getters/setters">

        public int getWeighting() {
            return weighting;
        }

        public void setWeighting(int weighting) {
            this.weighting = weighting;
        }

        public String getTestDirectory() {
            return testDirectory;
        }

        public void setTestDirectory(String testDirectory) {
            this.testDirectory = testDirectory;
        }

        public String getCorrectSolution() {
            return correctSolution;
        }

        public void setCorrectSolution(String correctSolution) {
            this.correctSolution = correctSolution;
        }

        public String getClassPath() {
            return classPath;
        }

        public void setClassPath(String classPath) {
            this.classPath = classPath;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        //</editor-fold>
    }

    /** Configuration options */
    private FunctionalityOptions options;

    /** Bundle containing the JUnit test files to run against the submission */
    private Bundle tests;

    /** Whether there were issues compiling the sample solution or tests */
    private boolean hasErrors;

    /**
     * Sets up the functionality stage ready to process a submission.
     *
     * @param options configuration options to use when running functionality
     *                tests
     */
    public Functionality(FunctionalityOptions options) {
        this.options = options;

        this.compileTests();
    }

    /**
     * Compile the sample solution and then compile the tests with the sample
     * solution.
     */
    public void compileTests() {
        tests = new Bundle(new File(options.testDirectory));
        Bundle solution = new Bundle(new File(options.correctSolution));

        /* Load output directories for the solution and the tests */
        Bundle solutionOutput;
        Bundle testOutput;
        try {
            solutionOutput = new Bundle();
            testOutput = new Bundle();
            /* Add the tests to the class path for execution */
            options.setClassPath(options.classPath
                    + System.getProperty("path.separator")
                    + testOutput.getUnmaskedPath());
        } catch (IOException e) {
            hasErrors = true;
            e.printStackTrace();
            return;
        }

        StringWriter output = new StringWriter();

        /* Compile the sample solution */
        Compiler.compile(Compiler.getSourceFiles(solution), options.classPath,
                solutionOutput.getUnmaskedPath(), output);

        /* Compile the tests with the sample solution */
        Compiler.compile(Compiler.getSourceFiles(tests),
                options.classPath
                        + System.getProperty("path.separator")
                        + solutionOutput.getUnmaskedPath(),
                testOutput.getUnmaskedPath(), output);
    }

    /**
     * Run the tests on a submission.
     *
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
    public Collection run(Collection submission) {
        if (hasErrors) {
            return submission;
        }
        if (!submission.getResults().is("extra_data.compilation.compiles")) {
            return submission;
        }

        /* No longer supported:
           Copy the included resources to the submission directory
        if (options.included != null && !options.included.isEmpty()) {
            try {
                submission.getWorking().copyFolder(new File(options.included));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */

        /* Class path contains dependencies and the compiled submission */
        String classPath = options.classPath
                + System.getProperty("path.separator")
                + submission.getWorking().getUnmaskedPath("bin");
        JSONArray testResults = (JSONArray) submission.getResults().get("tests");
        int totalNumTests = 0;
        JSONArray functionalityResults = new JSONArray();
        for (String className : tests.getClasses("")) {
            List<Data> results = JUnitRunner.runTests(className, classPath);
            /* Sort alphabetically by test class then test name */
            results.sort(Comparator.comparing(o -> ((String) o.get("name"))));

            for (Data result : results) {
                int testMultiplier = (Integer) result.get("weighting");
                /* e.g. a test worth 5 "units" will increase the total number of tests by 5 */
                totalNumTests += testMultiplier;
                functionalityResults.add(result);
            }
        }
        if (totalNumTests == 0) {
            return submission;
        }

        /* Mark awarded for passing a single test method (un-scaled by test multipliers) */
        final double individualTestWeighting = 1d / totalNumTests * options.weighting;
        int passingTests = 0;

        for (Object o : functionalityResults) {
            Data functionalityResult = (Data) o;
            boolean didPass = (Integer) functionalityResult.get("extra_data.passes") == 1;
            int testMultiplier = (Integer) functionalityResult.get("weighting");
            functionalityResult.set("status", didPass ? "passed" : "failed");
            passingTests += didPass ? 1 : 0;
//            functionalityResult.set("max_score", individualTestWeighting * testMultiplier);
            testResults.add(functionalityResult);
        }

        double total = (passingTests / 272.0) * 100;
        int grade = 1;
        if (total >= 85) {
            grade = 7;
        } else if (total >= 75) {
            grade = 6;
        } else if (total >= 65) {
            grade = 5;
        } else if (total >= 50) {
            grade = 4;
        } else if (total >= 30) {
            grade = 3;
        } else if (total >= 20) {
            grade = 2;
        }

        Data data = new Data();
        data.set("name", "Functionality Tests");
        data.set("score", grade);
        data.set("max_score", 7);
        data.set("output", "You passed " + passingTests + " out of 272 tests resulting in a grade of " + grade);
        data.set("visibility", "after_published");
        testResults.add(0, data);

        return submission;
    }
}
