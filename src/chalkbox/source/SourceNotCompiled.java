package chalkbox.source;

public class SourceNotCompiled extends RuntimeException {
    public SourceNotCompiled(String message) {
        super(message);
    }
}
