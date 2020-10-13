package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.collectors.GradescopeCollector;
import chalkbox.output.GradescopeOutput;
import org.json.simple.JSONArray;

public abstract class Engine implements Configuration {
    private String engine;
    private String courseCode;
    private String assignment;
    private String submission;
    private String outputFile;

    @Override
    public void validateConfig() throws ConfigFormatException {
        if (courseCode == null || courseCode.isEmpty()) {
            throw new ConfigFormatException("Missing course code");
        }
        if (assignment == null || assignment.isEmpty()) {
            throw new ConfigFormatException("Missing assessment identifier");
        }
        if (submission == null || submission.isEmpty()) {
            throw new ConfigFormatException("Missing submission path");
        }
        if (outputFile == null || outputFile.isEmpty()) {
            throw new ConfigFormatException("Missing output JSON path");
        }
    }

    public Collection collect() {
        Collection col = new GradescopeCollector().collect(submission, outputFile);
        col.getResults().set("tests", new JSONArray());
        return col;
    }

    public void output(Collection submission) {
        new GradescopeOutput().output(null, submission);
    }

    public abstract void run();

    @Override
    public String toString() {
        return String.format("%s %s", courseCode, assignment);
    }

    //<editor-fold desc="JavaBeans getters/setters">

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

    //</editor-fold>
}
