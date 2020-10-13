package chalkbox.java.checkstyle;

import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.Execution;
import chalkbox.api.common.ProcessExecution;
import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Configuration;
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

    public static class CheckstyleOptions implements Configuration {

        /**
         * Whether or not to run this stage
         */
        private boolean enabled = false;

        private int weighting;

        private String config;

        private String jar;

        private List<String> excluded;

        private double violationPenalty = 1;

        @Override
        public void validateConfig() throws ConfigFormatException {
            if (!enabled) {
                return;
            }

            /* Must have a configuration file */
            if (config == null || config.isEmpty()) {
                throw new ConfigFormatException(
                        "Missing config in Checkstyle stage");
            }

            /* Must have a Checkstyle JAR */
            if (jar == null || jar.isEmpty()) {
                throw new ConfigFormatException(
                        "Missing jar in Checkstyle stage");
            }

            /* Must have a weighting between 0 and 100 */
            if (weighting < 0 || weighting > 100) {
                throw new ConfigFormatException(
                        "Checkstyle weighting must be between 0 and 100");
            }
        }

        //<editor-fold desc="JavaBeans getters/setters">

        public int getWeighting() {
            return weighting;
        }

        public void setWeighting(int weighting) {
            this.weighting = weighting;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getJar() {
            return jar;
        }

        public void setJar(String jar) {
            this.jar = jar;
        }

        public List<String> getExcluded() {
            return excluded;
        }

        public void setExcluded(List<String> excluded) {
            this.excluded = excluded;
        }

        public double getViolationPenalty() {
            return violationPenalty;
        }

        public void setViolationPenalty(double violationPenalty) {
            this.violationPenalty = violationPenalty;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        //</editor-fold>
    }

    private CheckstyleOptions options;

    public Checkstyle(CheckstyleOptions options) {
        this.options = options;
    }

    public Collection run(Collection collection) {
        Data feedback = collection.getResults();

        // execute the checkstyle jar on the src directory
        ProcessExecution process;
        try {
            List<String> processArgs = new ArrayList<>();
            processArgs.add("java");
            processArgs.add("-jar");
            processArgs.add(options.jar);
            processArgs.add("-c");
            processArgs.add(options.config);
            processArgs.addAll(generateExcludedArgs(options.excluded));
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
            result.set("max_score", options.weighting);
            result.set("output", "IOError when running Checkstyle");
            tests.add(result);
            return collection;
        } catch (TimeoutException e) {
            e.printStackTrace();
            JSONArray tests = (JSONArray) feedback.get("tests");
            Data result = new Data();
            result.set("name", "Automated Style");
            result.set("score", 0);
            result.set("max_score", options.weighting);
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
                options.weighting - numViolations * options.violationPenalty));
        result.set("max_score", options.weighting);

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
