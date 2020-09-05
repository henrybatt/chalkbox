package chalkbox.output;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

public class GradescopeOutput {
//    @ConfigItem(key = "json")
//    public String json;

    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        for (Collection collection : collections) {
            Data results = collection.getResults();
            results.set("timestamp", System.currentTimeMillis() / 1000L);
            File jsonFile = new File("/autograder/results/results.json");
//            File jsonFile = new File(json + File.separator + results.get("sid") + ".json");
            JSONObject obj = new JSONObject();
            obj.put("score", 42);
            try {
                Files.write(jsonFile.toPath(), obj.toJSONString().getBytes());
            } catch (IOException e) {
                System.err.println("Unable to write output JSON file");
            }

//            try {
//                Files.write(jsonFile.toPath(), results.toString().getBytes());
//            } catch (IOException e) {
//                System.err.println("Unable to write json file for " + results.get("sid"));
//            }
        }
    }
}
