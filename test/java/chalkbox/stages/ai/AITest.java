package chalkbox.stages.ai;

import static org.junit.Assert.*;

import chalkbox.source.Submission;
import chalkbox.stages.Status;
import chalkbox.stages.Visibility;
import java.util.ArrayList;
import org.junit.Test;

public class AITest {

    @Test
    public void testDeclaredAiUsage() {
        var submission = new Submission(
            "test/resources/ai/declared",
            new ArrayList<>()
        );
        var results = new AI("ai/README.txt").run(submission);
        assertEquals("AI Declaration", results.overview().getName());
        assertEquals(Visibility.VISIBLE, results.overview().getVisibility());
        assertEquals(Status.PASSED, results.overview().getStatus());
    }

    @Test
    public void testDeclareNoAiUsage() {
        var submission = new Submission(
            "test/resources/ai/none",
            new ArrayList<>()
        );
        var results = new AI("ai/README.txt").run(submission);
        assertEquals("AI Declaration", results.overview().getName());
        assertEquals(Visibility.VISIBLE, results.overview().getVisibility());
        assertEquals(Status.PASSED, results.overview().getStatus());
    }

    @Test
    public void testMissing() {
        var submission = new Submission(
            "test/resources/ai/missing",
            new ArrayList<>()
        );
        var results = new AI("ai/README.txt").run(submission);
        assertEquals("AI Declaration", results.overview().getName());
        assertEquals(Visibility.VISIBLE, results.overview().getVisibility());
        assertEquals(Status.FAILED, results.overview().getStatus());
        assertEquals(-200.0, results.overview().getScore(), 0.1);
    }
}
