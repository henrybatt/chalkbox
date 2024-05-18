package chalkbox.java.junit;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.api.files.FileLoader;
import chalkbox.api.files.SourceFile;
import chalkbox.api.files.StringSourceFile;
import chalkbox.engines.ConfigFormatException;
import chalkbox.engines.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assesses submitted JUnit tests by running them against faulty implementations.
 */
public class JUnit {

    public static class JUnitOptions implements Configuration {

        /**
         * Whether to run this stage or not.
         */
        private boolean enabled = false;

        /**
         * Path to a directory containing the sample solution.
         */
        private String correctSolution;

        /**
         * Class path for student tests to be compiled with.
         */
        private String classPath;

        /**
         * Marks allocated to the JUnit stage.
         */
        private int weighting;

        /**
         * Path to a directory containing various broken sample solutions.
         */
        private String faultySolutions;

        /**
         * JUnit classes to execute.
         */
        private List<String> assessableTestClasses;

        /**
         * Checks this configuration and throws an exception if it is invalid.
         *
         * @throws ConfigFormatException if the configuration is invalid.
         */
        @Override
        public void validateConfig() throws ConfigFormatException {
            if (!enabled) {
                return;
            }

            /* Must have a faulty solutions directory */
            if (faultySolutions == null || faultySolutions.isEmpty()) {
                throw new ConfigFormatException("Missing faultySolutions in JUnit stage.");
            }

            /* Must have a list of assessable test classes */
            if (assessableTestClasses == null || assessableTestClasses.isEmpty()) {
                throw new ConfigFormatException("Missing assessableTestClasses in JUnit stage.");
            }

            /* Must have a weighting between 0 and 100 */
            if (weighting < 0 || weighting > 100) {
                throw new ConfigFormatException("JUnit weighting must be between 0 and 100.");
            }
        }

        //<editor-fold desc="JavaBeans getters/setters">

        public String getCorrectSolution() {
            return correctSolution;
        }

        public void setCorrectSolution(String correctSolution) {
            this.correctSolution = correctSolution;
        }

        public String getClassPath() {
            return classPath;
        }

        public void setClassPath(String classPath) {
            this.classPath = classPath;
        }

        public int getWeighting() {
            return weighting;
        }

        public void setWeighting(int weighting) {
            this.weighting = weighting;
        }

        public String getFaultySolutions() {
            return faultySolutions;
        }

        public void setFaultySolutions(String faultySolutions) {
            this.faultySolutions = faultySolutions;
        }

        public List<String> getAssessableTestClasses() {
            return assessableTestClasses;
        }

        public void setAssessableTestClasses(List<String> assessableTestClasses) {
            this.assessableTestClasses = assessableTestClasses;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        //</editor-fold>
    }

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(JUnit.class.getName());

    /** Configuration options. */
    private JUnitOptions options;

    /** Bundle containing compiled faulty implementations. */
    private Bundle solutionsOutput;

    /** Bundle containing compiled correct solution. */
    private Bundle solutionOutput;

    /** Class path containing dependencies and correct solution byte code. */
    private String solutionClassPath;

    /** Mapping of faulty implementation names to their respective class path. */
    private Map<String, String> classPaths = new TreeMap<>();

    /**
     * Total number of faulty implementations to run JUnit tests against,
     * excluding the correct solution if present.
     */
    private int numFaultySolutions;

    /**
     * Sets up the JUnit stage ready to process a submission.
     *
     * @param options configuration options to use when running JUnit stage.
     */
    public JUnit(JUnitOptions options) {
        this.options = options;

        createCompilationOutput();
        compileSolution();
        compileSolutions();
    }

    /**
     * Creates the compilation output directory.
     */
    private void createCompilationOutput() {
        /* Create a new temporary directory for compilation output. */
        Bundle compilationOutput;
        try {
            compilationOutput = new Bundle();
        } catch (IOException e) {
            LOGGER.severe("Unable to create compilation output directory.");
            return;
        }

        /* Create a subdirectory for broken solutions compilation. */
        try {
            solutionsOutput = compilationOutput.makeBundle("solutions");
        } catch (IOException e) {
            LOGGER.severe("Unable to create solutions directory in compilation output directory.");
            return;
        }

        /* Create a subdirectory for sample solution compilation. */
        try {
            solutionOutput = compilationOutput.makeBundle("solution");
        } catch (IOException e) {
            LOGGER.severe("Unable to create solution directory in compilation output directory.");
        }
    }

    /**
     * Compiles a single implementation and outputs the compiled byte code to the given location.
     *
     * @param source bundle containing source files to compile
     * @param name human readable name of the implementation to be compiled
     * @param output path to directory that will store compiled byte code
     * @param writer writer to write compile warnings/output to
     */
    private void compileSolution(Bundle source, String name, String output, StringWriter writer) {
        /* Collect all the source files to compile. */
        SourceFile[] files;
        try {
            files = source.getFiles(".java");
        } catch (IOException e) {
            LOGGER.severe("Unable to load the source files within solution.");
            return;
        }

        /* Compile the solution. */
        boolean compiled = Compiler.compile(Arrays.asList(files),
                options.classPath, output, writer);
        if (!compiled) {
            LOGGER.severe("Unable to compile solution: " + name);
            LOGGER.severe(writer.toString());
        }
        LOGGER.finest("Solution " + name + " Compilation Output");
        LOGGER.finest(writer.toString());
    }

    /**
     * Compile all the faulty implementations to test submitted JUnit tests against.
     */
    private void compileSolutions() {
        /* Collect the list of broken solution folders. */
        File solutionsFolder = new File(options.faultySolutions);
        /* Only include directories as faulty solutions (e.g. not .DS_Store). */
        File[] solutions = solutionsFolder.listFiles(File::isDirectory);
        if (solutions == null) {
            LOGGER.severe("Unable to load the folder of broken solutions.");
            return;
        }

        this.numFaultySolutions = solutions.length;
        // If "solution/" dir is in "solutions", subtract one from number of faulty solutions.
        for (File solution : solutions) {
            if (solution.getName().equals("solution")) {
                this.numFaultySolutions = solutions.length - 1;
                break;
            }
        }

        StringWriter writer;
        for (File solutionFolder : solutions) {
            String solutionName = FileLoader.truncatePath(solutionsFolder, solutionFolder);

            /* Get the folder for compilation output of this solution. */
            Bundle solutionBundle = new Bundle(new File(solutionFolder.getPath()));
            String solutionOut = solutionsOutput.getAbsolutePath(solutionFolder.getName());

            writer = new StringWriter();
            compileSolution(solutionBundle, solutionName, solutionOut, writer);

            /* Add an entry for this solution to the class path mapping. */
            classPaths.put(solutionName, options.classPath
                    + System.getProperty("path.separator") + solutionOut);
        }
    }

    /**
     * Compile the sample solution.
     */
    private void compileSolution() {
        Bundle solutionSource = new Bundle(new File(options.correctSolution));

        /* Compile the sample solution. */
        StringWriter writer = new StringWriter();
        compileSolution(solutionSource, "sample solution",
                solutionOutput.getUnmaskedPath(), writer);

        solutionClassPath = options.classPath + System.getProperty("path.separator")
                + solutionOutput.getUnmaskedPath();
    }

    /**
     * Runs the JUnit stage on the given submission.
     *
     * Firstly compiles the submitted tests, then if compilation was successful,
     * runs them against all the faulty implementations.
     *
     * @param submission submission to assess.
     * @return Given submission with extra test results indicating the results of the JUnit stage.
     */
    public Collection run(Collection submission) {
        compileTests(submission);
        runTests(submission);
        return submission;
    }

    /**
     * Compiles the submitted JUnit tests with the correct implementation.
     *
     * Sets "extra_data.junit.compiles" to true/false based on whether at least
     * one of the submitted tests compiled.
     *
     * @param submission submission containing tests to compile.
     * @return Given submission with extra test results.
     */
    private Collection compileTests(Collection submission) {
        Bundle source = submission.getSource();
        Bundle tests = null;
        try {
            tests = source.getBundle("test");
        } catch (NullPointerException npe) {
            /* Test directory was not submitted, leave 'tests' as null. */
        }

        StringJoiner output = new StringJoiner("\n");
        StringWriter error = new StringWriter();

        /* Compile each submitted test class individually. */
        boolean anyCompiles = false;
        boolean allCompiles = true;
        for (String className : options.assessableTestClasses) {
            boolean success = false;
            String fileName = className.replace(".", "/") + ".java";
            String packageName = className.substring(0, className.lastIndexOf("."));
            StringWriter compileOutput = new StringWriter();
            SourceFile file;
            try {
                file = tests.getFile(fileName); // Throws NPE if no test directory was found.
                StringSourceFile stringFile = StringSourceFile.copyOf(file);
                stringFile.replaceAll("^package (.+);(.*)", "package " + packageName + ";");
                String contents = stringFile.getContent();
                Pattern packageHeader = Pattern.compile("^package");
                Matcher matcher = packageHeader.matcher(contents);
                if (!matcher.find()) {
                    contents = "package " + packageName + ";" + System.lineSeparator() + contents;
                    stringFile = stringFile.update(contents);
                }
                List<SourceFile> files = new ArrayList<>();
                files.add(stringFile);
                boolean fileSuccess = Compiler.compile(files,
                        solutionClassPath,
                        submission.getWorking().getUnmaskedPath(),
                        compileOutput);
                if (fileSuccess) {
                    anyCompiles = true;
                }
                output.add("\u2705 JUnit test file `" + fileName + "` found.");
                if (fileSuccess) {
                    output.add("\u2705 JUnit test file `" + fileName + "` compiles.");
                    success = true;
                } else {
                    output.add("\u274C JUnit test file `" + fileName + "` does not compile.");
                }
                output.add(compileOutput.toString());
            } catch (FileNotFoundException | NullPointerException e) {
                error.write("\u274C JUnit test file `" + fileName + "` not found.\n");
            } catch (IOException e) {
                error.write("IO Compile Error - Please contact course staff.\n");
            }
            if (!success) {
                allCompiles = false;
            }
        }

        JSONArray testResults = (JSONArray) submission.getResults().get("tests");
        JSONObject junitResult = new JSONObject();
        junitResult.put("name", "JUnit compilation");
        junitResult.put("output_format", "md");
        junitResult.put("status", allCompiles ? "passed" : "failed");
        String visibleOutput = output.toString();
        if (!error.toString().isEmpty()) {
            visibleOutput += error.toString();
        }
        junitResult.put("output", visibleOutput);
        testResults.add(3, junitResult);

        /*
         * Run submitted JUnit tests against broken solutions even if one or
         * more test classes don't compile, as long as at least one does.
         */
        submission.getResults().set("extra_data.junit.compiles", anyCompiles);

        return submission;
    }

    /**
     * Runs the submitted JUnit tests against each faulty implementation.
     *
     * @param submission submission containing tests to run.
     * @return Given submission with extra test results, one for each faulty implementation.
     */
    private Collection runTests(Collection submission) {
        if (!submission.getResults().is("extra_data.junit.compiles")) {
            LOGGER.finest("Skipping running JUnit tests.");
            return submission;
        }

        LOGGER.finest("Running student tests");
        LOGGER.finest(options.assessableTestClasses.toString());
        File working = new File(submission.getSource().getUnmaskedPath());

        Map<String, Integer> passes = new HashMap<>();
//        Data junitInfo = new Data();
        for (String testClass : options.assessableTestClasses) {
            String classPath = solutionClassPath
                    + System.getProperty("path.separator")
                    + submission.getWorking().getUnmaskedPath();
            Data results = JUnitRunner.runTestsCombined(testClass, classPath);
            if (results.get("extra_data.passes") != null) {
                passes.put(testClass, Integer.parseInt(results.get("extra_data.passes").toString()));
            }
//            junitInfo.set("output", junitInfo.get("output") + "" +
//                    "\n==========\n" +
//                    testClass + " on Solution");
        }
        int totalSolutionPassed = passes.values().stream().mapToInt(Integer::intValue).sum();

        File solutionsFolder = new File(options.faultySolutions);
        int passingTests = 0;
        JSONArray tests = (JSONArray) submission.getResults().get("tests");
        for (String solution : classPaths.keySet()) {
            /* Class path for the particular solution. */
            String classPath = classPaths.get(solution)
                    + System.getProperty("path.separator")
                    + submission.getWorking().getUnmaskedPath();
            LOGGER.fine("The classpath used for faulty solution '" + solution
                    + "' was: " + classPath);

            /* JSON test result for this broken solution. */
            Data solutionResult = new Data();
            /* Results of the JUnit runner for each submitted test class. */
            List<Data> classResults = new ArrayList<>();
            /* Is the solution being tested against the correct implementation? */
            boolean isCorrectSolution = solution.equals("solution");

            for (String testClass : options.assessableTestClasses) {
                /* Run the JUnit tests. */
                Data results = JUnitRunner.runTestsCombined(testClass, classPath);
                results.set("extra_data.correct", false);
                if (results.get("extra_data.passes") != null) {
                    int passed = Integer.parseInt(results.get("extra_data.passes").toString());
                    if (passed < passes.get(testClass)) {
                        results.set("extra_data.correct", true);
                    }
                }
                classResults.add(results);
            }

            /* Mark awarded for correctly identifying a broken solution. */
            final double solutionWeighting = 1d / this.numFaultySolutions * options.weighting;

            /* Solutions whose name ends with _VISIBLE should be visible to students immediately. */
            if (solution.endsWith("_VISIBLE")) {
                /* Remove the _VISIBLE suffix from the final output. */
                solutionResult.set("name", "JUnit (" + solution.replaceAll("_VISIBLE", "") + ")");
                solutionResult.set("visibility", "visible");
            } else {
                solutionResult.set("name", "JUnit (" + solution + ")");
                solutionResult.set("visibility", "after_published");
            }
            /* The correct solution is not graded, but should still appear. */
//            if (!isCorrectSolution) {
//                solutionResult.set("score", 0);
//                solutionResult.set("max_score", solutionWeighting);
//            }
            /*
             * For each test class result JSON:
             *  - Concatenate the output of all the test classes.
             *  - Determine whether at least one test class was "correct".
             */
            StringJoiner joiner = new StringJoiner("\n");
            /* Find the total number of tests passed/failed for this solution. */
            int totalPassed = 0;
            int totalFailed = 0;
            for (Data classResult : classResults) {
                totalPassed += (Integer) classResult.get("extra_data.passes");
                totalFailed += (Integer) classResult.get("extra_data.fails");
            }

            if (!isCorrectSolution) {
                if (totalPassed < totalSolutionPassed) {
                    joiner.add("\n\u2705 Outcome: Your unit tests correctly detected that this "
                            + "was a faulty implementation.");
                    passingTests += 1;
                    solutionResult.set("status", "passed");
                } else {
                    joiner.add("\n\u274C Outcome: Your unit tests did not correctly detect that "
                            + "this was a faulty implementation.");
                    solutionResult.set("status", "failed");
                }
            }
            joiner.add("\nTests that passed when run against a correct "
                    + "implementation: **" + totalSolutionPassed + "**.");
            joiner.add("Tests that passed when run against this faulty "
                    + "implementation: **" + totalPassed + "**.");

            String description = solutionsFolder.getAbsolutePath() + "/" + solution + "/README.txt";
            if (Files.exists(Path.of(description))) {
                String content = "";
                try {
                    content = Files.readString(Path.of(description));
                } catch (IOException e) {
                    LOGGER.severe("Could not find solution description: " + description + ".");
                }
                joiner.add("\n### Scenario");
                joiner.add(content);
            }

            joiner.add("\n### Details");
            if (totalFailed > 0) {
                joiner.add("Tests which did not pass for this implementation:\n```text");
            }
            for (Data classResult : classResults) {
                String classOutput = (String) classResult.get("output");
                /* Don't add output if there is no output (""). */
                if (!classOutput.isEmpty()) {
                    joiner.add(classOutput);
                }
//                if (classResult.is("extra_data.correct") && !isCorrectSolution) {
//                    solutionResult.set("score", solutionWeighting);
//                }
            }
            joiner.add("```");
            solutionResult.set("output", joiner.toString());
            solutionResult.set("output_format", "md");

            tests.add(solutionResult);
        }

        double total = Math.ceil((passingTests / (float) numFaultySolutions) * options.weighting);

        Data data = new Data();
        data.set("name", "JUnit Tests");
        data.set("score", total);
        data.set("max_score", options.weighting);
        data.set("output", "You correctly identified bugs in " + passingTests
                + " out of " + numFaultySolutions
                + " buggy solutions.");
        data.set("visibility", "after_published");
        tests.add(1, data);

        return submission;
    }
}
