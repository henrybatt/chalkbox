package chalkbox.stages;

public enum Status {
    PASSED, FAILED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
