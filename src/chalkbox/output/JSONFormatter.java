package chalkbox.output;

import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSONFormatter reformates json output from CSSE1001Test using a provided python
 * script
 */
public class JSONFormatter {

    /**
     * Runs the given reformatting script in the required working directory
     *
     * @param formatterPath - The file path to the formatter script
     * @param includedPath - The file path to the included files
     * @param outputPath - The file path to the output file (the results file)
     */
    public static void runFormatter(String formatterPath, String includedPath, String outputPath, String visibleTests) {
        String PYTHON = "python3";
        ProcessExecution process;
        Map<String, String> environment = new HashMap<>();
        File working = new File(includedPath);

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, formatterPath, new File(outputPath).getAbsolutePath(),
                    visibleTests);
        } catch (IOException e) {
            System.err.println("Error occurred trying to spawn the test runner process");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error occurred");
            e.printStackTrace();
        }
    }
}
