package chalkbox.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "conformance",
        description = "Runs conformance over the input project ")
public class Conformance implements Runnable {
    @Mixin Shared shared = new Shared();

    @Override
    public void run() {

    }
}
