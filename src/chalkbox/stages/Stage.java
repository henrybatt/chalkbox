package chalkbox.stages;

import chalkbox.submission.Submission;

public interface Stage {
    Result run(Submission submission) throws StageException;
}
