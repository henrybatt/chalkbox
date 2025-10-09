package chalkbox.stages;

import chalkbox.source.Solution;
import chalkbox.source.Submission;

import java.util.List;

public interface Stage {
    Result run(Submission submission) throws StageException;

    Result run(Submission submission, Solution solution) throws StageException;

    Result run(Submission submission, List<Solution> solutions) throws StageException;
}
