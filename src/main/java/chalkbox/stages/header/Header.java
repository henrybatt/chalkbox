package chalkbox.stages.header;

import chalkbox.config.Config;
import chalkbox.config.ConfigException;
import chalkbox.source.Submission;
import chalkbox.stages.*;

@RegisterStage
public class Header
    extends BaseStage
    implements SubmissionOnlyStage, StageProducer {

    public Header() {
        super("Overview");
    }

    @Override
    public Stage build(Config config) throws ConfigException {
        return new Header();
    }

    @Override
    public StageResult run(Submission submission) throws StageException {
        // todo(mh): Add header section
        return StageResult.fromOverview(
            new Result("Info")
                .setStatus(Status.PASSED)
                .setVisibility(Visibility.VISIBLE)
                .setOutputFormat("html")
        );
    }
}
