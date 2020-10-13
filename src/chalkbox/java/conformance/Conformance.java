package chalkbox.java.conformance;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.FileLoader;
import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Configuration;
import chalkbox.java.conformance.comparator.ClassComparator;
import chalkbox.java.conformance.comparator.CodeComparator;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Checks whether a submission conforms exactly to the specified public API.
 *
 * Detects extra or missing files in a submission, compared to the expected
 * file structure. Uses class comparators to identify methods and members in the
 * submission that differ to those in the correct solution.
 */
public class Conformance {

    public static class ConformanceOptions implements Configuration {

        /**
         * Whether or not to run this stage
         */
        private boolean enabled = false;

        /**
         * Path of the correct solution to the assignment.
         *
         * Will be compiled into class files which are checked member-for-member
         * against classes in the the provided submission.
         */
        private String correctSolution;

        /**
         * Class path to use when compiling the sample solution.
         *
         * Any dependencies should be added here, e.g. JUnit.
         */
        private String classPath;

        /**
         * Path of the directory to compare against the provided submission.
         *
         * Extra and missing files will be flagged based on whether they appear
         * in this directory.
         */
        private String expectedStructure;

        /**
         * Number of marks allocated to the conformance check.
         *
         * Used when calculating the "score" for the conformance test result.
         */
        private int weighting;

        /**
         * Number of marks to subtract for each instance of non-conformance.
         *
         * Defaults to 1 mark per instance.
         */
        private double violationPenalty = 1;

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

            /*
             * Do not need classPath or correctSolution immediately - these are
             * set later.
             */

            /* Must have expected structure */
            if (expectedStructure == null || expectedStructure.isEmpty()) {
                throw new ConfigFormatException(
                        "Missing expectedStructure in conformance stage");
            }

            /* Must have a weighting between 0 and 100 */
            if (weighting < 0 || weighting > 100) {
                throw new ConfigFormatException(
                        "Conformance weighting must be between 0 and 100");
            }
        }

        //<editor-fold desc="JavaBeans getters/setters">

        public String getExpectedStructure() {
            return expectedStructure;
        }

        public void setExpectedStructure(String expectedStructure) {
            this.expectedStructure = expectedStructure;
        }

        public double getViolationPenalty() {
            return violationPenalty;
        }

        public void setViolationPenalty(double violationPenalty) {
            this.violationPenalty = violationPenalty;
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

        public int getWeighting() {
            return weighting;
        }

        public void setWeighting(int weighting) {
            this.weighting = weighting;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        //</editor-fold>
    }

    /**
     * Configuration options.
     */
    private ConformanceOptions options;

    /**
     * Mapping of class names to loaded classes for the expected structure.
     */
    private Map<String, Class> expectedClasses;

    /**
     * List of all files present in the expected structure.
     */
    private List<String> expectedFiles;

    /**
     * Sets up the conformance checker ready to check a submission.
     *
     * @param options configuration options to use when checking conformance
     * @throws IOException if loading the expected class files fails
     */
    public Conformance(ConformanceOptions options) throws IOException {
        /* Set the provided options */
        this.options = options;

        /* Load a list of all files expected to be found in a submission */
        this.expectedFiles = FileLoader.loadFiles(options.expectedStructure);

        /* Compile and store the Java classes from the expected structure */
        loadExpected();
    }

    /**
     * Loads the expected class files into the conformance checker
     */
    private void loadExpected() throws IOException {
        Bundle expected = new Bundle(new File(options.correctSolution));
        StringWriter output = new StringWriter();

        /* Load output directories for the solution and the tests */
        Bundle out;
        try {
            out = new Bundle();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* Compile the sample solution */
        Compiler.compile(Compiler.getSourceFiles(expected), options.classPath,
                out.getUnmaskedPath(), output);

        SourceLoader expectedLoader = new SourceLoader(out.getUnmaskedPath());
        try {
            expectedClasses = expectedLoader.getClassMap();
        } catch (ClassNotFoundException cnf) {
            throw new RuntimeException("Failed to load expected class");
        }
    }

    public Collection run(Collection submission) throws IOException {
        List<String> missing = new ArrayList<>();
        List<String> extra = new ArrayList<>();
        List<String> actual = FileLoader.loadFiles(submission.getSource().getUnmaskedPath());

        Data data = submission.getResults();
        JSONArray tests = (JSONArray) data.get("tests");
        Data result = new Data(); // test result representing conformance check
        result.set("name", "Conformance");
        result.set("output", "");
        result.set("score", 0);
        result.set("max_score", options.weighting);
        tests.add(result);

        for (String expected : expectedFiles) {
            if (!actual.contains(expected)) {
                missing.add(expected);
            }
        }

        for (String path : actual) {
            if (!expectedFiles.contains(path)) {
                extra.add(path);
            }
        }

        // Enforce deterministic order of list of missing/extra files
        Collections.sort(missing);
        Collections.sort(extra);

        result.set("output", result.get("output") + "Missing files:\n"
                + String.join("\n", missing) + "\n");
        result.set("output", result.get("output") + "Extra files:\n"
                + String.join("\n", extra) + "\n");

        // Only check classes for conformance if the submission compiles
        if (!data.is("extra_data.compilation.compiles")) {
            result.set("output", result.get("output")
                    + "Submission did not compile, not checking for conformance");
            return submission;
        }

        result.set("output", result.get("output") + "Class conformance:\n");

        SourceLoader submissionLoader = new SourceLoader(submission.getWorking()
                .getUnmaskedPath("bin"));
        Map<String, Class> submissionMap;
        try {
            submissionMap = submissionLoader.getClassMap();
        } catch (ClassNotFoundException|NoClassDefFoundError cnf) {
            result.set("output", result.get("output")
                    + "Unable to find a class in submission\n");
            cnf.printStackTrace();
            return submission;
        }

        int totalDifferences = 0;
        for (String className : expectedClasses.keySet()) {
            // Skip anon generated classes
            if (className.contains("$")) {
                continue;
            }

            Class expectedClass = expectedClasses.get(className);
            Class actualClass = submissionMap.get(className);

            if (expectedClass == null || actualClass == null) {
                result.set("output", result.get("output") + className
                        + " was not found (unable to load class)\n");
                continue;
            }

            CodeComparator<Class> comparator = new ClassComparator(expectedClass,
                    actualClass);
            if (comparator.hasDifference()) {
                // Class does not conform
                result.set("output", result.get("output") + className
                        + " does not conform:\n" + comparator.toString() + "\n");
                totalDifferences += comparator.getDifferenceCount();
            } else {
                // Class conforms
                result.set("output", result.get("output") + className
                        + " conforms\n");
            }
        }

        /*
         * Subtract [the number of differences multiplied by the penalty] from
         * the total weighting allocated to the conformance stage to yield the
         * final score.
         */
        result.set("score", Math.max(0, options.weighting
                - totalDifferences * options.violationPenalty));

        return submission;
    }
}
