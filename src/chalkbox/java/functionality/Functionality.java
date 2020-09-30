package chalkbox.java.functionality;

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
 * Requires {@link JavaCompilation} process to be executed first.
 * Checks if "extra_data.compilation.compiles" is true.
 */
public class Functionality {

    public static class FunctionalityOptions {

        /** Sample solution to compile tests with */
        private String correctSolution;

        /** Class path for tests to be compiled with */
        private String classPath;

        /** Number of marks allocated to the functionality stage */
        private int weighting;

        /** Path of JUnit test files */
        private String testDirectory;

        /**
         * Folder containing files to include in submission working directory
         * when running tests, e.g. save files used by initialiser class
         */
        private String included;

        public boolean isValid() {
            return testDirectory != null && !testDirectory.isEmpty()
                    && weighting != 0;
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

        public String getIncluded() {
            return included;
        }

        public void setIncluded(String included) {
            this.included = included;
        }

        //</editor-fold>
    }

    private FunctionalityOptions options;

    private Bundle tests;
    protected boolean hasErrors;

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
     * Run the tests on a submission
     */
    public Collection run(Collection submission) {
        if (hasErrors) {
            return submission;
        }
        if (!submission.getResults().is("extra_data.compilation.compiles")) {
            return submission;
        }
        /* Copy the included resources to the submission directory */
        if (options.included != null && !options.included.isEmpty()) {
            try {
                submission.getWorking().copyFolder(new File(options.included));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                totalNumTests++;
                functionalityResults.add(result);
            }
        }
        if (totalNumTests == 0) {
            return submission;
        }

        /* Mark awarded for passing a single test method */
        final double individualTestWeighting = 1d / totalNumTests
                * options.weighting;

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
