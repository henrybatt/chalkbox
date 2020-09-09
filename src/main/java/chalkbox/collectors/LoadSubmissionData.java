package chalkbox.collectors;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Prior;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles shared logic for loading submission data in the form of json
 * files as submission data.
 */
public class LoadSubmissionData {

    @ConfigItem(key = "json", description = "Location of data files", required = false)
    public String json;

    private static final String WARNING =
            "WARNING: Files exist in output data directory. " +
                    "These will likely be overridden. " +
                    "Are you sure you want to continue (y/n)? ";

    /**
     * Repeatedly warn and ask the user if they want to continue.
     */
    //todo(issue:#18): this needs to be able to be disabled in a 'non-interactive' mode
    private void promptWarning() {/*
        Scanner input = new Scanner(System.in);
        System.out.print(WARNING);
        String answer;
        do {
            answer = input.nextLine();
            if (answer.equals("n")) {
                System.exit(1);
            }
        } while (!answer.equals("y"));*/
    }

    /**
     * Load the submission data from a json file if it exists.
     * If no json file exists for a submission then creates a new data.
     *
     * The following key-values are assigned:
     * sid: set to the sid given to the method.
     * json: set to the path of the json file.
     */
    Data loadData(String sid) {
        return loadData(sid, new Data());
    }

    /**
     * Load the submission data from a json file if it exists.
     * If no json file exists for a submission then uses the given initial data.
     *
     * The following key-values are assigned:
     * sid: set to the sid given to the method.
     * json: set to the path of the json file.
     */
    Data loadData(String sid, Data initial) {
        String jsonPath = json + File.separator + sid + ".json";
        File file = new File(jsonPath);

        Data data;
        if (file.exists()) {
            try {
                data = new Data(new String(Files.readAllBytes(file.toPath())));
                //System.out.println("WARNING: Data file for " + sid + " loaded and likely to be overridden");
            } catch (IOException e) {
                System.err.println("Couldn't read data file: " + file);
                return initial;
            }
        } else {
            initial.set("sid", sid);
            initial.set("json", file.getPath());
            return initial;
        }

        //data.set("sid", sid);
        //data.set("json", file.getPath());

        return data;
    }
}
