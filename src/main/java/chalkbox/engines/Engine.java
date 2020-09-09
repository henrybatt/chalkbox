package chalkbox.engines;

public abstract class Engine {
    private String engine;
    private String courseCode;
    private String assignment;

    public boolean configIsValid() {
        return courseCode != null && !courseCode.isEmpty() && assignment != null
                && !assignment.isEmpty();
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

    public void run() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("%s %s", courseCode, assignment);
    }
}
