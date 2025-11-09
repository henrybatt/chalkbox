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
                        .setMaxScore(0)
                        .setScore(0)
                        .setOutputFormat("html");

        var declaration = submission.getAiDeclaration(path);
        if (declaration.isEmpty()) {
            result.appendOutput("<p>Your AI declaration is missing or empty. You <strong>must</strong> declare your AI usage.</p>");
            result.setStatus(Status.FAILED);
            result.setScore(-200);
        } else {
            var expectedDeclaration = "No generative AI tools were";
            if (declaration.toLowerCase().contains(expectedDeclaration.toLowerCase())) {
                result.appendOutput("""
                        <p><span style="color: blue; font-size: 20px;">🛈</span>
                        Your AI declaration indicates that no generative AI tools were used.
                        Ensure that this declaration is accurate. An inaccurate declaration may constitute academic misconduct.</p>
                        """);
            } else {
                result.appendOutput(String.format("""
                        <p><span style="color: blue; font-size: 20px;">🛈</span>
                        We have not found the exact declaration of "%s" therefore we have assumed that you have declared that you have used generative AI tools.
                        Ensure that your declaration is accurate. An inaccurate declaration may constitute academic misconduct.</p>
                        """, expectedDeclaration));
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
