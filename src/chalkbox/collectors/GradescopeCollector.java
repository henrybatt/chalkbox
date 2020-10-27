package chalkbox.collectors;

import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;

/**
 * Collects a submission from a single file path containing the files submitted
 * via Gradescope.
 */
public class GradescopeCollector {

    /**
     * Creates a new Collection representing a submission, containing files
     * in the given submission path.
     *
     * @param submissionPath path to locate submitted files
     * @param outputPath path to output the final JSON file after processing
     * @return a new collection based on the given submission
     */
    public static Collection collect(String submissionPath, String outputPath) {
        File submissionFolder = new File(submissionPath);

        Data metadata = new Data();

        metadata.set("root", submissionFolder.getPath());
        metadata.set("json", outputPath);

        return new Collection(metadata);
    }
}
