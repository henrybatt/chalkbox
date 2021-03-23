package chalkbox.api.common.java;

import chalkbox.api.collections.Data;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;

public class JUnitListener extends RunListener {
    private static class TestResult {
        private final String testName;
        private boolean visible = false;
        private boolean passed = true;
        private String output = "";

        public TestResult(String testName) {
            this.testName = testName;
        }
    }

    private List<TestResult> results;
    private TestResult currentResult;
    private int numFailed = 0;
    private StringBuilder output;

    public JUnitListener() {
        this.results = new ArrayList<>();
        this.output = new StringBuilder();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);

        this.currentResult = new TestResult(
                description.getTestClass().getSimpleName()
                + "." + description.getMethodName());

        /* Results of this test should be immediately visible */
        // TODO create a library containing a custom annotation for this
        if (description.getAnnotation(Deprecated.class) != null) {
            this.currentResult.visible = true;
        }
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);

        if (this.currentResult != null) {
            this.results.add(this.currentResult);
        }
        this.currentResult = null;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);

        if (this.currentResult != null) {
            this.output.append(failure);
            this.output.append("\n");
            this.currentResult.output = failure.toString() + "\n\n"
                    + failure.getTrace().replaceAll("\r\n", "\n");
            this.currentResult.passed = false;
        }
        this.numFailed++;
    }

    public Data getResultsForClass() {
        Data data = new Data();
        data.set("extra_data.passes", this.results.size() - this.numFailed);
        data.set("extra_data.fails", this.numFailed);
        data.set("extra_data.total", this.results.size());
        data.set("output", this.output.toString());
        return data;
    }

    public List<Data> getIndividualResults() {
        List<Data> results = new ArrayList<>();

        for (TestResult result : this.results) {
            Data data = new Data();
            data.set("extra_data.passes", result.passed ? 1 : 0);
            data.set("extra_data.fails", result.passed ? 0 : 1);
            data.set("extra_data.total", 1);
            data.set("output", result.output);
            data.set("name", result.testName);
            data.set("visibility", result.visible ? "visible" : "after_published");
            results.add(data);
        }
        return results;
    }
}
