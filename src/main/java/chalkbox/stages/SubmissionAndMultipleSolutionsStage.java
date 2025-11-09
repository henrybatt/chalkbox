package chalkbox.stages;

import chalkbox.source.Solution;
import chalkbox.source.Submission;

public interface SubmissionAndMultipleSolutionsStage extends Stage {
    default Type getType() {
        return Type.SUBMISSION_AND_MULTIPLE_SOLUTIONS;
    }

    default StageResult run(Submission submission) throws StageException {
        throw new StageException("Stage not implemented");
    }

    default StageResult run(Submission submission, Solution solution)
        throws StageException {
        throw new StageException("Stage not implemented");
    }
}
