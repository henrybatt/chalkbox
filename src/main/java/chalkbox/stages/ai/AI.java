package chalkbox.stages.ai;

import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.*;

import java.util.List;

public class AI implements Stage {
    private String path;

    public AI(String path) {
        this.path = path;
    }

    @Override
    public String getName() {
        return "AI Declaration";
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

        var declaration = submission.getAiDeclaration(path);
        if (declaration.isEmpty()) {
            result.appendOutput("<p>Your AI declaration is missing or empty. You <strong>must</strong> declare your AI usage.</p>");
            result.setStatus(Status.FAILED);
            result.setScore(-200);
        } else {
            if (declaration.contains("No generative AI tools were")) {
                result.appendOutput("<p><span style=\"color: blue; font-size: 20px;\">&#x1F6C8;</span> " +
                        "Your AI declaration indicates that no generative AI tools were used." +
                        "Ensure that this declaration is accurate. An inaccurate declaration may constitute academic misconduct.</p>\n");
            } else {
                result.appendOutput("<p><span style=\"color: blue; font-size: 20px;\">&#x1F6C8;</span> " +
                        "You have declared that you have used generative AI tools." +
                        "Ensure that your declaration is accurate. An inaccurate declaration may constitute academic misconduct.</p>\n");
            }
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
