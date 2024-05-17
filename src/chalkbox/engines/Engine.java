package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.collectors.GradescopeCollector;
import chalkbox.output.GradescopeOutput;
import org.json.simple.JSONArray;

/**
 * Base class for a ChalkBox engine.
 */
public abstract class Engine implements Configuration {
    /**
     * Fully-qualified class name of the concrete engine to run.
     */
    private String engine;

    /**
     * Course code identifier.
     */
    private String courseCode;

    /**
     * Assignment identifier.
     */
    private String assignment;

    /**
     * Path of the directory containing the submission.
     */
    private String submission;

    /**
     * Path to write the output JSON file to.
     */
    private String outputFile;

    @Override
    public void validateConfig() throws ConfigFormatException {
        if (courseCode == null || courseCode.isEmpty()) {
            throw new ConfigFormatException("Missing course code.");
        }
        if (assignment == null || assignment.isEmpty()) {
            throw new ConfigFormatException("Missing assessment identifier.");
        }
        if (submission == null || submission.isEmpty()) {
            throw new ConfigFormatException("Missing submission path.");
        }
        if (outputFile == null || outputFile.isEmpty()) {
            throw new ConfigFormatException("Missing output JSON path.");
        }
    }

    /**
     * Returns a new Collection representing the submission and its files.
     *
     * A "tests" key will be added to the results JSON object, with a value of an empty array.
     * Elements of this array are interpreted as individual tests by Gradescope.
     *
     * @return collected submission
     */
    public Collection collect() {
        Collection col = GradescopeCollector.collect(submission, outputFile);
        col.getResults().set("tests", new JSONArray());
        return col;
    }

    /**
     * Outputs the generated results JSON object to a file, able to be read by Gradescope.
     *
     * @param submission submission to output
     */
    public void output(Collection submission) {
        GradescopeOutput.output(submission);
    }

    /**
     * Runs the engine's processing on the submission.
     *
     * Will be called after the engine has been loaded and all configuration
     * has been validated successfully.
     */
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
