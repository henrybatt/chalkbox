package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.collectors.GradescopeCollector;
import chalkbox.output.GradescopeOutput;
import org.json.simple.JSONArray;

import java.util.Map;

public abstract class Engine {
    private String engine;
    private String courseCode;
    private String assignment;
    private Map<String, Integer> stages;
    private String submission;
    private String outputFile;

    public boolean configIsValid() {
        return courseCode != null && !courseCode.isEmpty()
                && assignment != null && !assignment.isEmpty()
                && submission != null && !submission.isEmpty()
                && outputFile != null && !outputFile.isEmpty()
                && stages != null && !stages.isEmpty();
    }

    public Collection collect() {
        Collection col = new GradescopeCollector().collect(submission, outputFile);
        col.getResults().set("tests", new JSONArray());
        return col;
    }

    public void output(Collection submission) {
        new GradescopeOutput().output(null, submission);
    }

    public int getWeighting(String stage) {
        return this.stages.get(stage);
    }

    public abstract void run();

    @Override
    public String toString() {
        return String.format("%s %s", courseCode, assignment);
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getAssignment() {
        return assignment;
    }

    public void setAssignment(String assignmentId) {
        this.assignment = assignmentId;
    }

    public Map<String, Integer> getStages() {
        return stages;
    }

    public void setStages(Map<String, Integer> stages) {
        this.stages = stages;
    }

    public String getSubmission() {
        return submission;
    }

    public void setSubmission(String submission) {
        this.submission = submission;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
}
