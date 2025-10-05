package chalkbox.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "codestyle",
        description = "Runs the codestyle over the project")
public class CodeStyle implements Runnable {
    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
    }
}
