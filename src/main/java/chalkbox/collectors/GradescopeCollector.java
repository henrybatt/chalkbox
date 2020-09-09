package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;

@Collector
public class GradescopeCollector extends LoadSubmissionData {

    public Collection collect() {
        File submissionFolder = new File("/autograder/submission");

        Data metadata = new Data();

        metadata.set("root", submissionFolder.getPath());
        metadata.set("json", "/autograder/results/results.json");

        return new Collection(metadata);
    }
}
