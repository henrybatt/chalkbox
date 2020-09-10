package chalkbox.collectors;

import chalkbox.api.annotations.Collector;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;

@Collector
public class GradescopeCollector extends LoadSubmissionData {

    public Collection collect(String submissionPath, String outputPath) {
        File submissionFolder = new File(submissionPath);

        Data metadata = new Data();

        metadata.set("root", submissionFolder.getPath());
        metadata.set("json", outputPath);

        return new Collection(metadata);
    }
}
