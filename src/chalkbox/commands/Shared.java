package chalkbox.commands;

import picocli.CommandLine;

public class Shared {
    @CommandLine.Option(names = { "-c", "--config" }, required = true, description = "Config file location")
    public String configFile;

    @CommandLine.Option(names = "--verbose", description = "Enable verbose output.")
    public boolean verbose;

    @CommandLine.Option(names= "--output", description = "Output file to write to")
    public String outputFile;
}