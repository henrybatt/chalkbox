package chalkbox.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "run",
        description = "Run one of the supported commands: CheckStyle, Conformance, Functionality, Mutation",
        subcommands = { Grade.class, Conformance.class, CodeStyle.class, Functionality.class, Mutation.class, TLC.class, CommandLine.HelpCommand.class })
public class Run implements Runnable {

    @Override
    public void run() {

    }
}