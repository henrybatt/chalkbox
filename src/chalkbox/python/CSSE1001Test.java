package chalkbox.python;

import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Class which runs the csse1001 testrunner script
 */
public class CSSE1001Test {

    public String PYTHON = "python3";

    /** a path to the runner script*/
    public String runner;

    /** a path to the included folder */
    public String included;


    public CSSE1001Test (String runner, String included) {
        this.runner = runner;
        this.included = included;
    }

    public Collection run(Collection collection) {
        Data feedback = collection.getResults();
        ProcessExecution process;
        Map<String, String> environment = new HashMap<>();
        environment.put("PYTHONPATH", included);
        File working = new File(collection.getWorking().getUnmaskedPath());

        try {
            collection.getWorking().copyFolder(new File(included));
        } catch (IOException e) {
            e.printStackTrace();
            feedback.set("test.error", "Unable to copy supplied directory");
            return collection;
        }

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, runner , "--json");
        } catch (IOException e) {
            System.err.println("Error occurred trying to spawn the test runner process (in json mode)");
            e.printStackTrace();
            feedback.set("test.error", "IOException occurred");
            return collection;
        } catch (TimeoutException e) {
            feedback.set("test.error", "Timed out executing tests");
            return collection;
        }

        String output = process.getOutput();
        feedback.set("test", new Data(output));

        System.err.println(process.getError());

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, runner);
        } catch (IOException e) {
            System.err.println("Error occurred trying to spawn the test runner process");
            e.printStackTrace();
            feedback.set("test.error", "IOException occurred");
            return collection;
        } catch (TimeoutException e) {
            feedback.set("test.error", "Timed out executing tests");
            return collection;
        }

        output = process.getOutput();
        feedback.set("output", output);

        return collection;
    }
}
