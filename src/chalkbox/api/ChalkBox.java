package chalkbox.api;

import chalkbox.api.config.ChalkboxConfig;
import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Engine;
import chalkbox.engines.EngineLoader;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChalkBox {
    private static final String USAGE = "Incorrect usage:" + System.lineSeparator()
            + "\tchalkbox <box file>" + System.lineSeparator()
            + "\tchalkbox help <class>";

    // i think the objects (in streams) are collections of student submissions
    private Map<String, List<Object>> streams = new HashMap<>();
    private ChalkboxConfig config;
    private boolean hasError;

    private PrintStream outputStream = System.out;

    /**
     * Constructor a new ChalkBox instance without loading any configuration.
     *
     * This should only be
     */
    private ChalkBox() {

    }

    /**
     * Construct a new ChalkBox instance based on the configuration file.
     *
     * @param configuration Chalkbox configuration settings.
     */
    public ChalkBox(ChalkboxConfig configuration) {
        this.config = configuration;

        /* Ensure that all the required classes are defined */
        for (String clazz : new String[]{"collector", "processor", "output"}) {
            if (!config.isSet(clazz)) {
                System.err.println("Configuration has no " + clazz + " class");
                hasError = true;
                return;
            }
        }
    }

    /**
     * Set the output stream of running the chalkbox to the given print stream.
     *
     * @param stream Stream to output run output to.
     */
    public void setOutput(PrintStream stream) {
        outputStream = stream;
    }


    public static void main(String[] args) throws ConfigFormatException {
        /*
        if (args.length == 2) {
            if (!args[0].equals("help")) {
                System.err.println(USAGE);
                return;
            }

            return;
        }
         */

        if (args.length != 1) {
            System.err.println(USAGE);
            return;
        }

        Engine engine = EngineLoader.load(args[0]);
        engine.run();
    }
}
