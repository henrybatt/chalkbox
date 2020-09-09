package chalkbox.engines;

public class ConfigFormatException extends Exception {
    public ConfigFormatException() {
        super();
    }

    public ConfigFormatException(String message) {
        super(message);
    }

    public ConfigFormatException(String message, Exception exception) {
        super(message, exception);
    }
}
