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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Processor to execute the Checkstyle tool on the submission.
 */
public class Checkstyle {

    public static class CheckstyleOptions implements Configuration {

        /** Whether or not to run this stage */
        private boolean enabled = false;

        /** Number of marks allocated to the Checkstyle stage */
        private int weighting;

        /** Path to the Checkstyle .xml configuration file */
        private String config;

        /** Path to the Checkstyle .jar */
        private String jar;

        /** List of paths to ignore when checking for style issues */
        private List<String> excluded;

        /** Number of marks to subtract for each Checkstyle violation */
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

    /** Configuration options */
    private CheckstyleOptions options;

    /**
     * Sets up the Checkstyle stage ready to process a submission.
     *
     * @param options configuration options to use when running Checkstyle
     */
    public Checkstyle(CheckstyleOptions options) {
        this.options = options;
    }

    public Collection run(Collection collection) {
        Data feedback = collection.getResults();

        JSONArray tests = (JSONArray) feedback.get("tests");
        Data result = new Data();
        result.set("name", "Automated Style");

        // if submission didn't compile, give 0 marks for automated style
        if (!feedback.is("extra_data.compilation.compiles")) {
            result.set("score", 0);
            result.set("max_score", options.weighting);
            result.set("output", "Submission did not compile, not checking automated style");
            tests.add(result);
            return collection;
        }

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

            process = Execution.runProcess(20000,
                    processArgs.toArray(String[]::new));
        } catch (IOException e) {
            e.printStackTrace();
            result.set("score", 0);
            result.set("max_score", options.weighting);
            result.set("output", "IOError when running Checkstyle");
            tests.add(result);
            return collection;
        } catch (TimeoutException e) {
            e.printStackTrace();
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

        // if Checkstyle didn't exit successfully, give 0 marks for automated style
        if (!checkstyleOutput.contains("Audit done.")) {
            result.set("score", 0);
            result.set("max_score", options.weighting);
            result.set("output", "\u274C Checkstyle did not exit successfully. " +
                    "This can indicate a syntax error or missing files." +
                    "\n### Details");
            System.out.println(process.getOutput());
            System.out.println(process.getError());
            result.set("output", result.get("output") + "\n```text\n" + checkstyleOutput + "\n```");
            result.set("output_format", "md");
            tests.add(result);
            return collection;
        }

        // count violations based on lines in output
        // subtract 2 for header/footer lines
        int numViolations = Math.max(0,
                checkstyleOutput.split("\n").length - 2);

        int grade = Math.max(0, 10 - numViolations);

        result.set("score", grade);
        result.set("max_score", 10);

        String formattedOutput = Arrays.stream(checkstyleOutput.split("\n"))
                .filter(n -> !n.contains("Starting audit") && !n.contains("Audit done"))
                        .map(n -> n.replace("[WARN] ", "\u274C "))
                                .collect(Collectors.joining("\n"));

        result.set("output",
                "A total of " + numViolations + " style violations."
                + "\n\n=============\n\n" + formattedOutput);
        tests.add(result);

        return collection;
    }

    /**
     * Transforms the given list of excluded directories to a list of command
     * line arguments for the Checkstyle tool.
     *
     * @param excluded list of excluded paths
     * @return list of command line arguments specifying excluded paths
     */
    private List<String> generateExcludedArgs(List<String> excluded) {
        List<String> args = new ArrayList<>();
        for (String s : excluded) {
            args.add("-e");
            args.add(s);
        }
        return args;
    }
}
