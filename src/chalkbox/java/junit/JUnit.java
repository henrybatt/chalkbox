package chalkbox.java.junit;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.common.java.JUnitRunner;
import chalkbox.api.files.FileLoader;
import chalkbox.api.files.SourceFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Logger;

public class JUnit {
    private static final Logger LOGGER = Logger.getLogger(JUnit.class.getName());

    /**
     * Path to a directory containing the sample solution
     */
    private String solutionPath;

    /**
     * Path to a directory containing various broken sample solutions
     */
    private String faultySolutionsPath;

    /**
     * JUnit classes to execute
     */
    private List<String> assessableTestClasses;

    /**
     * Class path for student tests to be compiled with
     */
    private String classPath;

    /**
     * Marks allocated to the JUnit stage
     */
    private int weighting;

    private Bundle solutionsOutput;
    private Bundle solutionOutput;

    private String solutionClassPath;
    private Map<String, String> classPaths = new TreeMap<>();
    private int numFaultySolutions;

    public JUnit(String solutionPath, String faultySolutionsPath,
            List<String> assessableTestClasses, String classPath,
            int weighting) {
        this.solutionPath = solutionPath;
        this.faultySolutionsPath = faultySolutionsPath;
        this.assessableTestClasses = assessableTestClasses;
        this.classPath = classPath;
        this.weighting = weighting;

        createCompilationOutput();
        compileSolution();
        compileSolutions();
    }

    /**
     * Creates the compilation output directory.
     */
    private void createCompilationOutput() {
        /* Create a new temporary directory for compilation output */
        Bundle compilationOutput;
        try {
            compilationOutput = new Bundle();
        } catch (IOException e) {
            LOGGER.severe("Unable to create compilation output directory");
            return;
        }

        /* Create a subdirectory for broken solutions compilation */
        try {
            solutionsOutput = compilationOutput.makeBundle("solutions");
        } catch (IOException e) {
            LOGGER.severe("Unable to create solutions directory in compilation output directory");
            return;
        }

        /* Create a subdirectory for sample solution compilation */
        try {
            solutionOutput = compilationOutput.makeBundle("solution");
        } catch (IOException e) {
            LOGGER.severe("Unable to create solution directory in compilation output directory");
        }
    }

    private void compileSolution(Bundle source, String name, String output,
                                 StringWriter writer) {
        /* Collect all the source files to compile */
        SourceFile[] files;
        try {
            files = source.getFiles(".java");
        } catch (IOException e) {
            LOGGER.severe("Unable to load the source files within solution");
            return;
        }

        /* Compile the solution */
        boolean compiled = Compiler.compile(Arrays.asList(files),
                classPath, output, writer);
        if (!compiled) {
            LOGGER.severe("Unable to compile solution: " + name);
            LOGGER.severe(writer.toString());
        }
        LOGGER.finest("Solution " + name + " Compilation Output");
        LOGGER.finest(writer.toString());
    }

    /**
     * Compile all the broken solutions to test students junit tests on
     */
    private void compileSolutions() {
        /* Collect the list of broken solution folders */
        File solutionsFolder = new File(this.faultySolutionsPath);
        /* Only include directories as faulty solutions (e.g. not .DS_Store) */
        File[] solutions = solutionsFolder.listFiles(File::isDirectory);
        if (solutions == null) {
            LOGGER.severe("Unable to load the folder of broken solutions");
            return;
        }

        this.numFaultySolutions = solutions.length;
        // If "solution/" dir is in "solutions", subtract one from number of
        // faulty solutions
        for (File solution : solutions) {
            if (solution.getName().equals("solution")) {
                this.numFaultySolutions = solutions.length - 1;
                break;
            }
        }


        StringWriter writer;
        for (File solutionFolder : solutions) {
            String solutionName = FileLoader.truncatePath(solutionsFolder, solutionFolder);

            /* Get the folder for compilation output of this solution */
            Bundle solutionBundle = new Bundle(new File(solutionFolder.getPath()));
            String solutionOut = solutionsOutput.getAbsolutePath(solutionFolder.getName());

            writer = new StringWriter();
            compileSolution(solutionBundle, solutionName, solutionOut, writer);

            /* Add an entry for this solution to the class path mapping */
            classPaths.put(solutionName, classPath
                    + System.getProperty("path.separator") + solutionOut);
        }
    }

    /**
     * Compile the sample solution.
     */
    private void compileSolution() {
        Bundle solutionSource = new Bundle(new File(solutionPath));

        /* Compile the sample solution */
        StringWriter writer = new StringWriter();
        compileSolution(solutionSource, "sample solution",
                solutionOutput.getUnmaskedPath(), writer);

        solutionClassPath = classPath + System.getProperty("path.separator")
                + solutionOutput.getUnmaskedPath();
    }

    public Collection run(Collection submission) {
        compileTests(submission);
        runTests(submission);
        return submission;
    }

    private Collection compileTests(Collection submission) {
        Bundle tests = submission.getSource().getBundle("test");

        StringJoiner output = new StringJoiner("\n");
        StringWriter error = new StringWriter();

        /* Compile each submitted test class individually */
        boolean anyCompiles = false;
        for (String className : assessableTestClasses) {
            String fileName = className.replace(".", "/") + ".java";
            StringWriter compileOutput = new StringWriter();
            SourceFile file;
            try {
                file = tests.getFile(fileName);
                List<SourceFile> files = new ArrayList<>();
                files.add(file);
                boolean fileSuccess = Compiler.compile(files,
                        solutionClassPath,
                        submission.getWorking().getUnmaskedPath(),
                        compileOutput);
                if (fileSuccess) {
                    anyCompiles = true;
                }
                output.add("JUnit test file " + fileName + " found");
                output.add("JUnit test file " + fileName
                        + (fileSuccess ? " compiles" : " does not compile"));
                output.add(compileOutput.toString());
            } catch (FileNotFoundException e) {
                error.write("JUnit test file " + fileName + " not found\n");
            } catch (IOException e) {
                error.write("IO Compile Error - Please contact course staff\n");
            }
        }

        JSONArray testResults = (JSONArray) submission.getResults().get("tests");
        JSONObject junitResult = new JSONObject();
        junitResult.put("name", "JUnit compilation");
        String visibleOutput = "Output:\n" + output.toString();
        if (!error.toString().isEmpty()) {
            visibleOutput += "\nError:\n" + error.toString();
        }
        junitResult.put("output", visibleOutput);
        testResults.add(junitResult);

        /*
        Run submitted JUnit tests against broken solutions even if one or
        more test classes don't compile, as long as at least one does.
         */
        submission.getResults().set("extra_data.junit.compiles", anyCompiles);

        return submission;
    }

    private Collection runTests(Collection submission) {
        if (!submission.getResults().is("extra_data.junit.compiles")) {
            LOGGER.finest("Skipping running JUnit tests");
            return submission;
        }

        LOGGER.finest("Running student tests");
        LOGGER.finest(assessableTestClasses.toString());
        File working = new File(submission.getSource().getUnmaskedPath());

        Map<String, Integer> passes = new HashMap<>();
        for (String testClass : assessableTestClasses) {
            String classPath = solutionClassPath
                    + System.getProperty("path.separator")
                    + submission.getWorking().getUnmaskedPath();
            Data results = JUnitRunner.runTestsCombined(testClass, classPath);
            if (results.get("extra_data.passes") != null) {
                passes.put(testClass, Integer.parseInt(results.get("extra_data.passes").toString()));
            }
        }

        JSONArray tests = (JSONArray) submission.getResults().get("tests");
        for (String solution : classPaths.keySet()) {
            /* Class path for the particular solution */
            String classPath = classPaths.get(solution)
                    + System.getProperty("path.separator")
                    + submission.getWorking().getUnmaskedPath();

            /* JSON test result for this broken solution */
            Data solutionResult = new Data();
            /* Results of the JUnit runner for each submitted test class */
            List<Data> classResults = new ArrayList<>();

            for (String testClass : assessableTestClasses) {
                /* Run the JUnit tests */
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

            /* Mark awarded for correctly identifying a broken solution */
            final double solutionWeighting = 1d / this.numFaultySolutions
                    * this.weighting;

            solutionResult.set("name", "JUnit (" + solution + ")");
            solutionResult.set("visibility", "after_due_date");
            solutionResult.set("score", 0);
            solutionResult.set("max_score", solutionWeighting);
            /*
                For each test class result JSON:
                - Concatenate the output of all the test classes
                - Determine whether at least one test class was "correct"
             */
            StringJoiner joiner = new StringJoiner("\n");
            for (Data classResult : classResults) {
                String classOutput = (String) classResult.get("output");
                /* Don't add output if there is no output ("") */
                if (!classOutput.isEmpty()) {
                    joiner.add(classOutput);
                }
                if (classResult.is("extra_data.correct")) {
                    solutionResult.set("score", solutionWeighting);
                }
            }
            solutionResult.set("output", joiner.toString());
            // TODO more informative output on how many tests passed/failed

            tests.add(solutionResult);
        }

        return submission;
    }
}
