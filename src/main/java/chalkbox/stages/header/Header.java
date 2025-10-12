package chalkbox.stages.header;

import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.*;

import java.util.List;

public class Header implements Stage {
    @Override
    public String getName() {
        return "Overview";
    }

    @Override
    public Type getType() {
        return Type.SUBMISSION_ONLY;
    }

    @Override
    public StageResult run(Submission submission) throws StageException {
        // todo(mh): Add header section
        return StageResult.fromOverview(new Result("Info")
                .setStatus(Status.PASSED)
                .setVisibility(Visibility.VISIBLE)
                .setOutputFormat("html")
        );
    }

    @Override
    public StageResult run(Submission submission, Solution solution) throws StageException {
        return null;
    }

    @Override
    public StageResult run(Submission submission, List<Solution> solutions) throws StageException {
        return null;
    }
}
