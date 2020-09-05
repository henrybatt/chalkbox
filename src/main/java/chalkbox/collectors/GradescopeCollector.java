package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.annotations.DataSet;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Collector
public class GradescopeCollector extends LoadSubmissionData {

    @DataSet
    public List<Collection> collect(Map<String, String> config) {
        List<Collection> collections = new ArrayList<>();
        File submissionFolder = new File("/autograder/submission");

//        Data metadata = loadData(submissionFolder.getName());
        Data metadata = new Data();

        metadata.set("root", submissionFolder.getPath());
        metadata.set("json", "/autograder/results/results.json");

        Collection collection = new Collection(metadata);
        collections.add(collection);
        return collections;
    }
}
