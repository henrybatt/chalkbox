package chalkbox.stages.conformance;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.FileLoader;
import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Configuration;
import chalkbox.stages.StageResult;
import chalkbox.stages.conformance.comparator.ClassComparator;
import chalkbox.stages.conformance.comparator.CodeComparator;
import chalkbox.submission.Submission;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Checks whether a submission conforms exactly to the specified public API.
 *
 * Detects extra or missing files in a submission, compared to the expected
 * file structure. Uses class comparators to identify methods and members in the
 * submission that differ to those in the correct solution.
 */
public class Conformance {

    private List<String> filesToIgnore;

    private String classPath;

    /**
     * Sets up the conformance checker ready to check a submission.
     */
    public Conformance(String classPath, List<String> ignoreMatches) throws IOException {
        this.filesToIgnore = ignoreMatches;
        this.classPath = classPath;
    }


    private SourceLoader loaderWithDeps(String directory) throws IOException {
        URL[] urls = Arrays.stream(classPath.split(":"))
                .map(e -> {
                    try {
                        return new URL("file://" + e);
                    } catch (MalformedURLException ex) {
                        throw new RuntimeException(ex);
                    }
                }).toArray(URL[]::new);
        URLClassLoader loader = new URLClassLoader(urls);
        return new SourceLoader(directory, loader);
    }

    /**
     * Runs the conformance stage against the provided submission.
     *
     * @param submission submission to check for conformance
     * @return given submission with extra test result for conformance results
     * @throws IOException if the submission's compiled source files cannot be
     * found
     */
    public StageResult run(Submission submission, Solution solution) throws IOException {
        var missing = new ArrayList<String>();
        var extra = new ArrayList<String>();
        // todo(mh): apple the exceptions to this via wildcard globbing.
        var actual = FileLoader.loadFiles(submission.getSrcFolder());
        // todo(mh): apply the exceptions to this via wildcard globbing.
        var expectedFiles = FileLoader.loadFiles(solution.getSrcFolder());

        var result = new StageResult();

        for (var expected : expectedFiles) {
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

        if (missing.isEmpty()) {
            result.appendComment("✅ No missing files");
        } else {
            result.appendComment("❌ Missing files\n");
            result.appendComment(String.join("\n", missing));
        }

        if (extra.isEmpty()) {
            result.appendComment("✅ No extra files");
        } else {
            result.appendComment("⚠️ Extra files\n(note: this is a sanity check for you, if you intended to upload these files for example AI documentation or other useful files, ignore this warning)\n\n");
            result.appendComment( String.join("\n", extra) + "\n\n");
        }

        // todo(mh): Calculate grade, might need to add a pass/fail flag

        if (!submission.compiles()) {
            result.appendComment("❌ Submission did not compile, cannot check for conformance");
            return result;
        }

        var submissionLoader = loaderWithDeps(submission.getBuildPath());
        Map<String, Class> submissionMap;
        try {
            submissionMap = submissionLoader.getClassMap();
        } catch (ClassNotFoundException|NoClassDefFoundError cnf) {
            result.set("name", "Conformance");
            result.set("output", "❌ Unable to find a class in submission");
            return result;
        }

        int totalDifferences = 0;
        for (String className : expectedClasses.keySet()) {
            // Skip anon generated classes
            if (className.contains("$")) {
                continue;
            }

            result = new Data();
            result.set("name", "Conformance: " + className);
            result.set("output", "");
            tests.add(result);

            // TODO: VERY TEMP
            if (className.contains("PlayerManager") || className.contains("BeanWorld")) {
                result.set("output", "Conformance temporarily not checked due to bug (course staff are working to fix this).");
                continue;
            }

            Class expectedClass = expectedClasses.get(className);
            Class actualClass = submissionMap.get(className);

            if (expectedClass == null || actualClass == null) {
                result.set("output", "❌ `" + className
                        + "` was not found (unable to load class)\n");
                result.set("output_format", "md");
                result.set("status", "failed");
                totalDifferences += 1; // 1-difference penalty for class not found
                continue;
            }

            CodeComparator<Class> comparator = new ClassComparator(expectedClass,
                    actualClass);
            if (comparator.hasDifference()) {
                // Class does not conform
                result.set("output", "❌ `" + className
                        + "` does not conform:\n\n```text\n" + comparator.toString() + "```");
                result.set("output_format", "md");
                result.set("status", "failed");
                totalDifferences += comparator.getDifferenceCount();
            } else {
                // Class conforms
                result.set("output", "✅ `" + className
                        + "` conforms.\n");
                result.set("output_format", "md");
                result.set("status", "passed");
            }
        }

        return submission;
    }
}
