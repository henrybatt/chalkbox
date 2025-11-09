package chalkbox.stages;

import chalkbox.source.Solution;
import chalkbox.source.Submission;
import java.util.List;

public interface SubmissionAndSolutionStage extends Stage {
    default Type getType() {
        return Type.SUBMISSION_AND_SOLUTION;
    }

    default StageResult run(Submission submission) throws StageException {
        throw new StageException("Stage not implemented");
    }

    default StageResult run(Submission submission, List<Solution> solution)
        throws StageException {
        throw new StageException("Stage not implemented");
    }
}
