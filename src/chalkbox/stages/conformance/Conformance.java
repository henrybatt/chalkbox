package chalkbox.stages.conformance;

import chalkbox.api.files.FileLoader;
import chalkbox.source.Source;
import chalkbox.stages.Result;
import chalkbox.stages.Stage;
import chalkbox.stages.StageException;
import chalkbox.stages.StageResult;
import chalkbox.stages.conformance.comparator.ClassComparator;
import chalkbox.stages.conformance.comparator.CodeComparator;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import com.google.common.flogger.FluentLogger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;

/**
 * Checks whether a submission conforms exactly to the specified public API.
 *
 * Detects extra or missing files in a submission, compared to the expected
 * file structure. Uses class comparators to identify methods and members in the
 * submission that differ to those in the correct solution.
 */
public class Conformance implements Stage {
    private List<String> ignoreWildcards;
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * Sets up the conformance checker ready to check a submission.
     */
    public Conformance(List<String> ignoreWildcards){
        this.ignoreWildcards = ignoreWildcards;
    }

    @Override
    public Result run(Submission submission) throws StageException {
        return null;
    }

    @Override
    public Result run(Submission submission, List<Solution> solutions) throws StageException {
        return null;
    }

    /**
     * Runs the conformance stage against the provided submission.
     *
     * @param submission submission to check for conformance
     * @return given submission with extra test result for conformance results
     */
    public Result run(Submission submission, Solution solution) throws StageException {
        var result = new StageResult();
        var missing = new ArrayList<String>();
        var extra = new ArrayList<String>();

        try {
            var compilation = submission.compileSrc();
            if (!compilation.success()) {
                result.appendComment("Unable to compile: " + compilation.output());
                return result;
            }
        } catch (IOException e) {
            result.appendComment("Submission did not compile, not checking automated style");
            result.appendComment(e.toString());
            return result;
        }

        try {
            var compilation = solution.compileSrc();
            if (!compilation.success()) {
                logger.atSevere().log(compilation.output());
                throw new StageException("Unable to compile solution");
            }
        } catch (IOException e) {
            throw new StageException(e.toString());
        }

        var actual = removeMatchingFiles(FileLoader.loadFiles(submission.getSrcFolder()), ignoreWildcards);
        var expectedFiles = removeMatchingFiles(FileLoader.loadFiles(solution.getSrcFolder()), ignoreWildcards);

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
            result.appendComment("❌ Missing files");
            for (var missingFile : missing) {
                result.appendComment(missingFile);
            }

        }

        if (extra.isEmpty()) {
            result.appendComment("✅ No extra files");
        } else {
            result.appendComment("⚠️ Extra files\n(note: this is a sanity check for you, if you intended to upload these files for example AI documentation or other useful files, ignore this warning)\n");
            result.appendComment(String.join("\n", extra) + "\n\n");
        }

//        if (!submission.compiles()) {
//            result.appendComment("❌ Submission did not compile, cannot check for conformance");
//            return result;
//        }

        Map<String, Class> submissionClasses;
        try {
            submissionClasses = getSourceClasses(submission);
        } catch (IOException | ClassNotFoundException e) {
            result.appendComment("❌ Unable to find a class in submission");
            return result;
        }

        Map<String, Class> expectedClasses = null;
        try {
            expectedClasses = getSourceClasses(solution);
        } catch (IOException | ClassNotFoundException e) {
            throw new StageException("Unable to load solution: " + e);
        }


        int totalDifferences = 0;
        for (String className : expectedClasses.keySet()) {
            // Skip anon generated classes
            if (className.contains("$")) {
                continue;
            }

            var expectedClass = expectedClasses.get(className);
            var actualClass = submissionClasses.get(className);

            if (expectedClass == null || actualClass == null) {
                result.appendComment("❌ `" + className + "` was not found (unable to load class)\n");
                totalDifferences += 1; // 1-difference penalty for class not found
                continue;
            }

            var comparator = new ClassComparator(expectedClass, actualClass);
            if (comparator.hasDifference()) {
                // Class does not conform
                result.appendComment("❌ `" + className + "` does not conform:\n\n```text\n" + comparator + "```");
                totalDifferences += comparator.getDifferenceCount();
            } else {
                // Class conforms
                result.appendComment("✅ `" + className + "` conforms.\n");
            }
        }

        // todo(mh): calc score

        return result;
    }

    private Map<String, Class> getSourceClasses(Source source) throws IOException, ClassNotFoundException {
        SourceLoader loader = source.getSrcLoader();
        return loader.getClassMap();
    }

    private List<String> removeMatchingFiles(List<String> files, List<String> globs) {
        var fs = FileSystems.getDefault();
        var filtered = new ArrayList<String>();
        for (var glob : globs) {
            var matcher = fs.getPathMatcher(glob);
            for (var file : files) {
                if (!matcher.matches(Path.of(file))) {
                    filtered.add(file);
                }
            }
        }
        return filtered;
    }
}
