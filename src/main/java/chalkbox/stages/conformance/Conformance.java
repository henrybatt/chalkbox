package chalkbox.stages.conformance;

import chalkbox.api.files.FileLoader;
import chalkbox.config.Config;
import chalkbox.config.ConfigException;
import chalkbox.source.Solution;
import chalkbox.source.Source;
import chalkbox.source.Submission;
import chalkbox.stages.*;
import chalkbox.stages.conformance.comparator.ClassComparator;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import org.github.gestalt.config.reflect.TypeCapture;

/**
 * Checks whether a submission conforms exactly to the specified public API.
 *
 * Detects extra or missing files in a submission, compared to the expected
 * file structure. Uses class comparators to identify methods and members in the
 * submission that differ to those in the correct solution.
 */
@RegisterStage
public class Conformance
    extends BaseStage
    implements SubmissionAndSolutionStage, StageProducer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private List<String> ignoreWildcards;

    public Conformance() {
        super("Conformance");
    }

    /**
     * Sets up the conformance checker ready to check a submission.
     */
    public Conformance(List<String> ignoreWildcards) {
        this();
        this.ignoreWildcards = ignoreWildcards;
    }

    @Override
    public Stage build(Config config) throws ConfigException {
        return new Conformance(
            config.getConfig(
                "conformance.ignored",
                new TypeCapture<List<String>>() {}
            )
        );
    }

    /**
     * Runs the conformance stage against the provided submission.
     *
     * @param submission submission to check for conformance
     * @return given submission with extra test result for conformance results
     */
    public StageResult run(Submission submission, Solution solution)
        throws StageException {
        var result = new Result(getName());
        var missing = new ArrayList<String>();
        var extra = new ArrayList<String>();

        try {
            var compilation = submission.compileSrc();
            if (!compilation.success()) {
                result.appendOutput(
                    "Submission did not compile, not checking automated style"
                );
                result.appendOutput(compilation.output());
                return StageResult.fromOverview(result);
            }
        } catch (IOException e) {
            result.appendOutput(
                "Submission did not compile, not checking automated style"
            );
            result.appendOutput(e.toString());
            return StageResult.fromOverview(result);
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

        var actual = removeMatchingFiles(
            FileLoader.loadFiles(submission.getSrcFolder()),
            ignoreWildcards
        );
        var expectedFiles = removeMatchingFiles(
            FileLoader.loadFiles(solution.getSrcFolder()),
            ignoreWildcards
        );

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
            result.appendOutput("✅ No missing files\n");
        } else {
            result.appendOutput("❌ Missing files\n");
            for (var missingFile : missing) {
                result.appendOutput(missingFile);
            }
        }

        if (extra.isEmpty()) {
            result.appendOutput("✅ No extra files\n");
        } else {
            result.appendOutput(
                "⚠️ Extra files\n(note: this is a sanity check for you, if you intended to upload these files for example AI documentation or other useful files, ignore this warning)\n"
            );
            result.appendOutput(String.join("\n", extra) + "\n\n");
        }

        Map<String, Class> expectedClasses = null;
        try {
            expectedClasses = getSourceClasses(solution);
        } catch (IOException | ClassNotFoundException e) {
            throw new StageException("Unable to load solution: " + e);
        }

        Map<String, Class> submissionClasses;
        try {
            submissionClasses = getSourceClasses(submission);
        } catch (IOException | ClassNotFoundException e) {
            result.appendOutput("❌ Unable to find a class in submission");
            return StageResult.fromOverview(result);
        }

        int totalDifferences = 0;
        for (String className : expectedClasses.keySet()) {
            // Skip anon generated classes
            //            if (className.contains("$")) {
            //                continue;
            //            }

            var expectedClass = expectedClasses.get(className);
            var actualClass = submissionClasses.get(className);

            if (expectedClass == null || actualClass == null) {
                result.appendOutput(
                    "❌ `" +
                    className +
                    "` was not found (unable to load class)\n"
                );
                totalDifferences += 1; // 1-difference penalty for class not found
                continue;
            }

            var comparator = new ClassComparator(expectedClass, actualClass);
            if (comparator.hasDifference()) {
                // Class does not conform
                result.appendOutput(
                    "❌ `" +
                    className +
                    "` does not conform:\n\n```text\n" +
                    comparator +
                    "```\n"
                );
                totalDifferences += comparator.getDifferenceCount();
            } else {
                // Class conforms
                result.appendOutput("✅ `" + className + "` conforms.\n");
            }
        }

        // todo(mh): calc score

        return StageResult.fromOverview(result);
    }

    private Map<String, Class> getSourceClasses(Source source)
        throws IOException, ClassNotFoundException {
        SourceLoader loader = source.getSrcLoader();
        return loader.getClassMap();
    }

    private List<String> removeMatchingFiles(
        List<String> files,
        List<String> globs
    ) {
        var fs = FileSystems.getDefault();
        var filtered = new ArrayList<String>(files);
        var toBeRemoved = new ArrayList<String>();
        for (var glob : globs) {
            var matcher = fs.getPathMatcher(glob);
            for (var file : files) {
                if (!matcher.matches(Path.of(file))) {
                    toBeRemoved.add(file);
                }
            }
        }
        filtered.removeAll(toBeRemoved);
        return filtered;
    }
}
