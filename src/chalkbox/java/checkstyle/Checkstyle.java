package chalkbox.java.checkstyle;

import chalkbox.api.annotations.Processor;
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
@Processor
public class  Checkstyle {
    private String jar;
    private String config;
    private List<String> excluded;

    public Checkstyle(String jar, String config, List<String> excluded) {
        this.jar = jar;
        this.config = config;
        this.excluded = excluded;
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
            result.set("name", "checkstyle");
            result.set("score", 0);
            result.set("output", "IOError when running Checkstyle");
            tests.add(result);
            return collection;
        } catch (TimeoutException e) {
            e.printStackTrace();
            JSONArray tests = (JSONArray) feedback.get("tests");
            Data result = new Data();
            result.set("name", "checkstyle");
            result.set("score", 0);
            result.set("output", "Timed out when running Checkstyle");
            tests.add(result);
            return collection;
        }

        // get the absolute base path of src
        String basePath = Paths.get(collection.getSource().getUnmaskedPath()).toAbsolutePath().toString();

        JSONArray tests = (JSONArray) feedback.get("tests");
        Data result = new Data();
        result.set("name", "checkstyle");
        result.set("score", 1); // TODO processing of Checkstyle results
        // replace the base path and set the output
        result.set("output", process.getOutput().replace(basePath, ""));
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
