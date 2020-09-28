package chalkbox.java.conformance;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.FileLoader;
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
    /**
     * Path of the correct solution to the assignment.
     *
     * Will be compiled into class files which are checked member-for-member
     * against classes in the the provided submission.
     */
    private final String correctSolution;

    /**
     * Class path to use when compiling the sample solution.
     *
     * Any dependencies should be added here, e.g. JUnit.
     */
    private final String classPath;

    /**
     * Number of marks allocated to the conformance check.
     *
     * Used when calculating the "score" for the conformance test result.
     */
    private int weighting;

    private Map<String, Class> expectedClasses;
    private List<String> expectedFiles;

    /**
     *
     * @param correctSolution path of the correct solution to the assignment
     * @param expectedStructure path of the directory to compare against the
     *                          provided submission. Extra and missing files
     *                          will be flagged based on whether they appear
     *                          in this directory
     * @param classPath class path to use when compiling the sample solution
     * @param weighting number of marks allocated to the conformance check
     * @throws IOException if loading the expected class files fails
     */
    public Conformance(String correctSolution, String expectedStructure,
            String classPath, int weighting) throws IOException {
        this.correctSolution = correctSolution;
        this.classPath = classPath;
        this.expectedFiles = FileLoader.loadFiles(expectedStructure);
        this.weighting = weighting;

        loadExpected();
    }

    /**
     * Loads the expected class files into the conformance checker
     */
    private void loadExpected() throws IOException {
        Bundle expected = new Bundle(new File(correctSolution));
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
        Compiler.compile(Compiler.getSourceFiles(expected), classPath,
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
        result.set("max_score", this.weighting);
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

        result.set("score", Math.max(0, this.weighting - totalDifferences));

        return submission;
    }
}
