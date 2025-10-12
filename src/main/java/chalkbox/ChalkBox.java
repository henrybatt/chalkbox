package chalkbox;

import chalkbox.commands.Run;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "chalkbox", version="2.0", mixinStandardHelpOptions = true, subcommands = { Run.class })
public class ChalkBox {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ChalkBox()).execute(args);
        System.exit(exitCode);
    }
}