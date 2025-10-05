package chalkbox.commands;

import picocli.CommandLine;

public class Shared {
    @CommandLine.Option(names = { "-c", "--config" }, required = true, description = "Config file location")
    private String configFile;

    @CommandLine.Option(names = "--verbose", description = "Enable verbose output.")
    boolean verbose;
}