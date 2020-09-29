package chalkbox.java.checkstyle;

import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Processor to execute the Checkstyle tool on the submission.
 */

public class Checkstyle {
    private String jar;
    private String config;
    private List<String> excluded;
    private int weighting;
    private double violationPenalty;

    public Checkstyle(String jar, String config, List<String> excluded,
            int weighting, double violationPenalty) {
        this.jar = jar;
        this.config = config;
        this.excluded = excluded;
        this.weighting = weighting;
        this.violationPenalty = violationPenalty;
    }

    public Collection run(Collection collection) {
        Data feedback = collection.getResults();

        // execute the checkstyle jar on the src directory
        ProcessExecution process;
        try {
            List<String> processArgs = new ArrayList<>();
            processArgs.add("java");
            processArgs.add("-jar");
            processArgs.add(this.jar);
            processArgs.add("-c");
            processArgs.add(this.config);
            processArgs.addAll(generateExcludedArgs(this.excluded));
            processArgs.add(collection.getSource().getUnmaskedPath("src"));

            process = Execution.runProcess(10000,
                    processArgs.toArray(String[]::new));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO improve how this is done, maybe refactor Data
            JSONArray tests = (JSONArray) feedback.get("tests");
            Data result = new Data();
            result.set("name", "Automated Style");
            result.set("score", 0);
            result.set("max_score", this.weighting);
            result.set("output", "IOError when running Checkstyle");
            tests.add(result);
            return collection;
        } catch (TimeoutException e) {
            e.printStackTrace();
            JSONArray tests = (JSONArray) feedback.get("tests");
            Data result = new Data();
            result.set("name", "Automated Style");
            result.set("score", 0);
            result.set("max_score", this.weighting);
            result.set("output", "Timed out when running Checkstyle");
            tests.add(result);
            return collection;
        }

        // get the absolute base path of src
        String basePath = Paths.get(collection.getSource().getUnmaskedPath()).toAbsolutePath().toString();

        // replace the base path to make output easier to read
        String checkstyleOutput = process.getOutput().replace(basePath, "");

        // count violations based on lines in output
        // subtract 2 for header/footer lines
        int numViolations = Math.max(0,
                checkstyleOutput.split("\n").length - 2);

        JSONArray tests = (JSONArray) feedback.get("tests");
        Data result = new Data();
        result.set("name", "Automated Style");
        result.set("score", Math.max(0,
                this.weighting - numViolations * this.violationPenalty));
        result.set("max_score", this.weighting);

        result.set("output", checkstyleOutput);
        tests.add(result);

        return collection;
    }

    private List<String> generateExcludedArgs(List<String> excluded) {
        List<String> args = new ArrayList<>();
        for (String s : excluded) {
            args.add("-e");
            args.add(s);
        }
        return args;
    }
}
