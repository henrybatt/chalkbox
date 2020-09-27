package chalkbox.api.common.java;

import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Utility class to execute a JUnit test.
 */
public class JUnitRunner {
    private static final String JUNIT_RUNNER = "org.junit.runner.JUnitCore";

    /**
     * Run a JUnit test with the name className and a given classPath.
     *
     * @param className Name of the JUnit class to execute.
     * @param classPath Class path for the JUnit execution.
     * @param working Working directory to execute tests within.
     *
     * @return The json output of executing a JUnit test
     */
    public static Data runTest(String className, String classPath, File working) {
        Data results = new Data();

        /* Execute a JUnit process */
        ProcessExecution process;
        try {
            process = Execution.runProcess(working, 60000, "java", "-cp",
                    classPath, JUNIT_RUNNER, className);
        } catch (IOException e) {
            e.printStackTrace();
            results.set("score", 0);
            results.set("output", "Test running IO Error - see tutor");
            return results;
        } catch (TimeoutException e) {
            results.set("score", 0);
            results.set("output", "Timed out");
            return results;
        }

        /* Consume the std out and std error */
        String output = process.getOutput();
        String errors = process.getError();

        /* Parse the JUnit output */
        JUnitParser jUnit;
        try {
            jUnit = JUnitParser.parse(output);
        } catch (IOException io) {
            io.printStackTrace();
            results.set("score", 0);
            results.set("output", "Test parsing IO Error - see tutor");
            return results;
        } catch (JUnitParseException p) {
            System.err.println(output);
            System.err.println(errors);
            p.printStackTrace();
            results.set("score", 0);
            results.set("output", p.getMessage());
            return results;
        }

        results.set("extra_data.passes", jUnit.getPasses());
        results.set("extra_data.fails", jUnit.getFails());
        results.set("extra_data.total", jUnit.getTotal());
        results.set("output", jUnit.formatOutput());

        return results;
    }
}
