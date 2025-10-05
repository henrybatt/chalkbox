package chalkbox.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "mutation",
        description = "Runs mutation over the input project")
public class Mutation implements Runnable {
    @Mixin Shared shared = new Shared();

    @Override
    public void run() {

    }
}
