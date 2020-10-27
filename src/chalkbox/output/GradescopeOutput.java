package chalkbox.output;

import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Outputs a submission's feedback to a JSON file in Gradescope format.
 */
public class GradescopeOutput {

    /**
     * Writes the JSON data in the given submission to a file.
     *
     * The file path written to is the value of the "json" key in the
     * submission's results Data instance.
     *
     * @param submission submission to output
     */
    public static void output(Collection submission) {
        Data results = submission.getResults();
        File jsonFile = new File((String) results.get("json"));

        // The below fields are not part of the Gradescope format, but still
        // needed during the processing pipeline (for now).
        results.delete("root");
        results.delete("json");

        try {
            Files.write(jsonFile.toPath(), results.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Unable to write output JSON file");
        }
    }
}
