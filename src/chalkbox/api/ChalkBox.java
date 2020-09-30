package chalkbox.api;

import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Engine;
import chalkbox.engines.EngineLoader;

public class ChalkBox {
    private static final String USAGE = "Incorrect usage:" + System.lineSeparator()
            + "\tchalkbox <box file>" + System.lineSeparator()
            + "\tchalkbox help <class>";

    public static void main(String[] args) throws ConfigFormatException {

        if (args.length != 1) {
            System.err.println(USAGE);
            return;
        }

        Engine engine = EngineLoader.load(args[0]);
        engine.run();
    }
}
