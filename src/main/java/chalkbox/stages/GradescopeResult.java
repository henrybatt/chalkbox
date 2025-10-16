package chalkbox.stages;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GradescopeResult {
    private double score;
    @SerializedName("execution_time")
    private double executionTime;
    private String output;
    @SerializedName("output_format")
    private String outputFormat;
    @SerializedName("test_output_format")
    private String testOutputFormat;
    @SerializedName("test_name_format")
    private String testNameFormat;
    private Visibility visibility = Visibility.AFTER_PUBLISHED;
    @SerializedName("stdout_visibility")
    private Visibility stdoutVisibility = Visibility.HIDDEN;
    private List<Result> tests = new ArrayList<>();

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }


    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getTestOutputFormat() {
        return testOutputFormat;
    }

    public void setTestOutputFormat(String testOutputFormat) {
        this.testOutputFormat = testOutputFormat;
    }

    public String getTestNameFormat() {
        return testNameFormat;
    }

    public void setTestNameFormat(String testNameFormat) {
        this.testNameFormat = testNameFormat;
    }

    public Visibility getStdoutVisibility() {
        return stdoutVisibility;
    }

    public void setStdoutVisibility(Visibility stdoutVisibility) {
        this.stdoutVisibility = stdoutVisibility;
    }

    public List<Result> getTests() {
        return tests;
    }

    public void setTests(List<Result> tests) {
        this.tests = tests;
    }

    /**
     * Adds a stage to the result.
     *
     * Adds the score of each StageResult.Overview().Score() to the total score
     * @param result
     */
    public void add(StageResult result) {
        var overview = result.overview();
        if (overview == null) {
            return;
        }
        this.tests.add(overview);
        this.score += overview.getScore();

        if (result.results() == null) {
            return;
        }
        this.tests.addAll(result.results());
    }
}
