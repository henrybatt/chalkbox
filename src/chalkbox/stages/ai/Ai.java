package chalkbox.stages.ai;

import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.*;

import java.util.List;

public class Ai implements Stage {
    @Override
    public String getName() {
        return "Ai Declaration";
    }

    @Override
    public Type getType() {
        return Type.SUBMISSION_ONLY;
    }

    @Override
    public StageResult run(Submission submission) throws StageException {
        var result = new Result(getName())
                        .setStatus(Status.PASSED)
                        .setVisibility(Visibility.VISIBLE)
                        .setOutputFormat("html");

        var declaration = submission.getAiDeclaration();
        if (declaration.isEmpty()) {
            result.appendOutput("<p>You have not declared if you have or have not used Ai</p>");
            result.setStatus(Status.FAILED);
        } else {
            if (declaration.contains("No generative AI tools were utilised")) {
                result.appendOutput("<p><span style=\"color: blue; font-size: 20px;\">&#x1F6C8;</span> You have declared that you did not use Ai in your submission</p>\n");
            } else {
                result.appendOutput("<p><span style=\"color: blue; font-size: 20px;\">&#x1F6C8;</span> You have declared that you <b>did<b> use Ai in your submission</p>\n");
            }
            result.appendOutput(String.format("""
                        <h2>Ai Declaration</h2>
                        <code>
                        %s
                        </code>
                        """, declaration));
        }

        return StageResult.fromOverview(result);
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
