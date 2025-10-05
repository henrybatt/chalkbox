package chalkbox.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "functionality",
        description = "Runs functionality over the input project")
public class Functionality implements Runnable {
    @Mixin Shared shared = new Shared();

    @Override
    public void run() {

    }
}
