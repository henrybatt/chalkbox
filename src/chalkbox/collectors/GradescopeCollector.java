package chalkbox.collectors;

import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;

public class GradescopeCollector {

    public Collection collect(String submissionPath, String outputPath) {
        File submissionFolder = new File(submissionPath);

        Data metadata = new Data();

        metadata.set("root", submissionFolder.getPath());
        metadata.set("json", outputPath);

        return new Collection(metadata);
    }
}
