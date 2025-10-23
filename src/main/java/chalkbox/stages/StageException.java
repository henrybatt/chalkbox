package chalkbox.stages;

public class StageException extends RuntimeException {
    public StageException(String message) {
        super(message);
    }

    public StageException(Throwable cause) {
        super(cause);
    }
}
