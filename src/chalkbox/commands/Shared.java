package chalkbox.commands;

import picocli.CommandLine;

public class Shared {
    @CommandLine.Option(names = { "-c", "--config" }, required = true, description = "Config file location")
    public String configFile;

    @CommandLine.Option(names = "--verbose", description = "Enable verbose output.")
    public boolean verbose;

    @CommandLine.Parameters(description = "File path to the submission root folder (should contain the src and test folders)")
    public String submissionPath;
}