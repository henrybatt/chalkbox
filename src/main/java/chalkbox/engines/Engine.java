package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.collectors.GradescopeCollector;
import chalkbox.output.GradescopeOutput;

public abstract class Engine {
    private String engine;
    private String courseCode;
    private String assignment;
    private String submission;
    private String outputFile;

    public boolean configIsValid() {
        return courseCode != null && !courseCode.isEmpty()
                && assignment != null && !assignment.isEmpty()
                && submission != null && !submission.isEmpty()
                && outputFile != null && !outputFile.isEmpty();
    }

    public Collection collect() {
        return new GradescopeCollector().collect(submission, outputFile);
    }

    public void output(Collection submission) {
        new GradescopeOutput().output(null, submission);
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
