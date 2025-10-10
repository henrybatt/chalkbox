package chalkbox.stages;

import chalkbox.source.Solution;
import chalkbox.source.Submission;

import java.util.List;

public interface Stage {
    StageResult run(Submission submission) throws StageException;

    StageResult run(Submission submission, Solution solution) throws StageException;

    StageResult run(Submission submission, List<Solution> solutions) throws StageException;
}
