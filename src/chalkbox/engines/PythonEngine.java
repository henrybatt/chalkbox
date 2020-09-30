package chalkbox.engines;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;
import chalkbox.python.CSSE1001Test;
import chalkbox.python.RenameSubmissions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class PythonEngine extends Engine {

    private String fileName;
    private String expectedExtension;
    private String runner;
    private String included;
    private String formatter;

    @Override
    public void run() {
        System.out.println("Running Python engine");

        Collection submission = super.collect();
        //submission.setWorking(new Bundle(new File(this.getSubmission())));



        try {
            submission.getWorking().copyFolder(new File(this.getSubmission()));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }


        //Rename the student submission
        RenameSubmissions rename = new RenameSubmissions(fileName, expectedExtension);
        try {
            submission = rename.run(submission);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        //Run the test runner
        CSSE1001Test test = new CSSE1001Test(runner, included);
        submission = test.run(submission);

        super.output(submission);

        /*
            Reformat the python
         */

        String PYTHON = "python3";
        ProcessExecution process;
        Map<String, String> environment = new HashMap<>();
        File working = new File(included);

        try {
            process = Execution.runProcess(working, environment, 10000,
                    PYTHON, formatter, new File(this.getOutputFile()).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error occurred trying to spawn the test runner process");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error occurred");
            e.printStackTrace();
        }


    }

    public String getRunner() {
        return runner;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExpectedExtension() {
        return expectedExtension;
    }

    public void setExpectedExtension(String expectedExtension) {
        this.expectedExtension = expectedExtension;
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    public String getIncluded() {
        return included;
    }

    public void setIncluded(String included) {
        this.included = included;
    }

}
