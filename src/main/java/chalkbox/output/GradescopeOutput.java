package chalkbox.output;

import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

public class GradescopeOutput {

    @Output
    public void output(PrintStream stream, Collection submission) {
        Data results = submission.getResults();

        // The below fields are not part of the Gradescope format, but still
        // needed during the processing pipeline (for now).
        results.delete("root");
        results.delete("json");

        File jsonFile = new File("/autograder/results/results.json");
        try {
            Files.write(jsonFile.toPath(), results.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Unable to write output JSON file");
        }
    }
}
