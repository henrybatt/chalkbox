package chalkbox.stages.ai;

import chalkbox.config.Config;
import chalkbox.config.ConfigException;
import chalkbox.source.Submission;
import chalkbox.stages.*;

@RegisterStage
public class AI
    extends BaseStage
    implements SubmissionOnlyStage, StageProducer {

    private String path;

    public AI() {
        super("AI Declaration");
    }

    public AI(String path) {
        this();
        this.path = path;
    }

    @Override
    public Stage build(Config config) throws ConfigException {
        return new AI(
            config.getConfig("ai.path", "ai/README.txt", String.class)
        );
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
            result.appendOutput(
                "<p>Your AI declaration is missing or empty. You <strong>must</strong> declare your AI usage.</p>"
            );
            result.setStatus(Status.FAILED);
            result.setScore(-200);
        } else {
            var expectedDeclaration = "No generative AI tools were";
            if (
                declaration
                    .toLowerCase()
                    .contains(expectedDeclaration.toLowerCase())
            ) {
                result.appendOutput(
                    """
                    <p><span style="color: blue; font-size: 20px;">🛈</span>
                    Your AI declaration indicates that no generative AI tools were used.
                    Ensure that this declaration is accurate. An inaccurate declaration may constitute academic misconduct.</p>
                    """
                );
            } else {
                result.appendOutput(
                    String.format(
                        """
                        <p><span style="color: blue; font-size: 20px;">🛈</span>
                        We have not found the exact declaration of "%s" therefore we have assumed that you have declared that you have used generative AI tools.
                        Ensure that your declaration is accurate. An inaccurate declaration may constitute academic misconduct.</p>
                        """,
                        expectedDeclaration
                    )
                );
            }
        }

        return StageResult.fromOverview(result);
    }
}
