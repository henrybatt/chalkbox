package chalkbox.engines;

/**
 * Denotes a class with fields that are set by reading from a configuration file.
 */
public interface Configuration {
    /**
     * Checks whether the values of the fields obtained from the configuration file are valid.
     *
     * @throws ConfigFormatException if the configuration is not valid.
     */
    void validateConfig() throws ConfigFormatException;
}
