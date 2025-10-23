package chalkbox.stages.conformance;

import chalkbox.api.files.FileLoader;
import chalkbox.source.Solution;
import chalkbox.source.Source;
import chalkbox.source.Submission;
import chalkbox.stages.*;
import chalkbox.stages.conformance.comparator.ClassComparator;
import com.google.common.flogger.FluentLogger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Checks whether a submission conforms at minimum to the specified public API.
 *
 * Detects missing files in a submission, compared to the expected
 * file structure. Uses class comparators to identify methods and members in the
 * submission that differ to those in the correct solution.
 */
public class ConformanceLite implements Stage {
    private static final String name = "Conformance (Lite)";

    private final List<String> ignoreWildcards;
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * Sets up the conformance checker ready to check a submission.
     */
    public ConformanceLite(List<String> ignoreWildcards){
        this.ignoreWildcards = ignoreWildcards;
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
        return null;
    }

    @Override
    public StageResult run(Submission submission, List<Solution> solutions) throws StageException {
        return null;
    }

    /**
     * Runs the conformance stage against the provided submission.
     *
     * @param submission submission to check for conformance
     * @return given submission with extra test result for conformance results
     */
    public StageResult run(Submission submission, Solution solution) throws StageException {
        var result = new Result(name).setVisibility(Visibility.HIDDEN).setOutputFormat("html");
        var missing = new ArrayList<String>();
        var extra = new ArrayList<String>();

        try {
            var compilation = submission.compileSrc();
            if (!compilation.success()) {
                result.appendOutput("Submission did not compile, not checking conformance");
                result.appendOutput(compilation.output());
                return StageResult.fromOverview(result);
            }
        } catch (IOException e) {
            result.appendOutput("Submission did not compile, not checking conformance");
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

        var actual = removeMatchingFiles(FileLoader.loadFiles(submission.getSrcFolder()), ignoreWildcards);
        var expectedFiles = removeMatchingFiles(FileLoader.loadFiles(solution.getSrcFolder()), ignoreWildcards);

        for (var expected : expectedFiles) {
            if (!actual.contains(expected)) {
                missing.add(expected);
            }
        }

        // Enforce deterministic order of list of missing/extra files
        Collections.sort(missing);
        Collections.sort(extra);

        if (missing.isEmpty()) {
            result.appendOutput("<p>✅ No missing files<p/>");
        } else {
            result.appendOutput("""
                    <div style="
                        background-color: #ffe5e5;
                        border: 1px solid #cc0000;
                        padding: 15px;
                        margin-bottom: 20px;
                        border-radius: 5px;
                        font-family: Arial, sans-serif;
                        font-size: 16px;
                    ">
                        <strong style="font-weight: bold; color: #cc0000;">Warning:</strong> Having missing files will most likely cause an issue in latter stages.
                    """);
            result.appendOutput("<p>Missing files</p>");
            result.appendOutput("<ul>");
            for (var missingFile : missing) {
                result.appendOutput(String.format("<li>%s</li>", missingFile));
            }
            result.appendOutput("</ul>");
            result.appendOutput("</div>");
        }
        return StageResult.fromOverview(result);
    }

    private List<String> removeMatchingFiles(List<String> files, List<String> globs) {
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
