package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.java.checkstyle.Checkstyle;
import chalkbox.java.compilation.JavaCompilation;
import chalkbox.java.conformance.Conformance;
import chalkbox.java.junit.JUnit;
import chalkbox.java.functionality.Functionality;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * ChalkBox engine for Java submissions.
 *
 * Supports several processing "stages", including:
 * <ul>
 * <li>Checking conformance to a public API.</li>
 * <li>Running JUnit tests against the submission.</li>
 * <li>Checking adherence to a style guide by running the Checkstyle tool.</li>
 * <li>Assessing submitted JUnit tests by running against faulty implementations.</li>
 * </ul>
 */
public class JavaEngine extends Engine implements Configuration {

    /** The jar file containing code provided to students. */
    public static final String PROVIDED_JAR = "provided.jar";

    /** Path to the correct implementation. */
    private String correctSolution;

    /**
     * Paths to libraries required as dependencies when running the engine.
     * For example, JUnit and Hamcrest.
     */
    private List<String> dependencies;

    /* Configuration options for each stage. */
    private Conformance.ConformanceOptions conformance;
    private Functionality.FunctionalityOptions functionality;
    private JUnit.JUnitOptions junit;
    private Checkstyle.CheckstyleOptions checkstyle;

    /** Logger to record events in processing submissions. */
    private static final Logger LOGGER = Logger.getLogger(JUnit.class.getName());

    @Override
    public void validateConfig() throws ConfigFormatException {
        super.validateConfig();

        /* All stages are optional. Validate each stage that is present. */

        if (this.conformance != null) {
            this.conformance.validateConfig();
        }

        if (this.functionality != null) {
            this.functionality.validateConfig();
        }

        if (this.junit != null) {
            this.junit.validateConfig();
        }

        if (this.checkstyle != null) {
            this.checkstyle.validateConfig();
        }
    }

    @Override
    public void run() {
        System.out.println("Running CSSE7023 engine.");

        Collection submission = super.collect();

        /* Convert list of dependencies to a single classpath string. */
        String classPath = dependenciesToClasspath(this.dependencies);

        JavaCompilation compilation = new JavaCompilation(classPath);
        submission = compilation.compile(submission);

        if (this.conformance != null && this.conformance.isEnabled()) {
            this.conformance.setCorrectSolution(correctSolution);
            this.conformance.setClassPath(classPath);
            try {
                Conformance conformance = new Conformance(this.conformance);
                submission = conformance.run(submission);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        if (this.checkstyle != null && this.checkstyle.isEnabled()) {
            Checkstyle checkstyle = new Checkstyle(this.checkstyle);
            submission = checkstyle.run(submission);
        }

        if (this.functionality != null && this.functionality.isEnabled()) {
            this.functionality.setCorrectSolution(correctSolution);
            this.functionality.setClassPath(classPath);
            Functionality test = new Functionality(this.functionality);
            submission = test.run(submission);
        }

        /*
         * Remove code provided to students, which is in PROVIDED_JAR, from list of dependencies.
         * Ensures that it is not included in classpath when testing students' JUnit tests.
         * This is necessary so that students' JUnit tests can run against faulty implementations.
         * The classpath for the faulty implementations is set in the JUnit class.
         *
         * This needs to be the last stage run on the submission.
         * The previous stages need to have "provided.jar" in the classpath.
         */
        List<String> dependenciesForJUnitTests = new ArrayList<>(dependencies);
        dependenciesForJUnitTests.removeIf(path -> path.contains(PROVIDED_JAR));
        classPath = dependenciesToClasspath(dependenciesForJUnitTests);
        LOGGER.fine("The classpath to be used when running students' JUnit tests is: "
                + classPath);

        // Run tests of students' JUnit tests against faulty implementations of the solution.
        if (this.junit != null && this.junit.isEnabled()) {
            this.junit.setCorrectSolution(correctSolution);
            this.junit.setClassPath(classPath);
            JUnit jUnit = new JUnit(this.junit);
            submission = jUnit.run(submission);
        }

        super.output(submission);
    }

    /**
     * Joins the paths in the given list by the classpath separator.
     *
     * @param dependencies paths to join, can be relative paths
     * @return single classpath string
     */
    private String dependenciesToClasspath(List<String> dependencies) {
        StringJoiner joiner = new StringJoiner(System.getProperty("path.separator"));
        for (String dependency : dependencies) {
            File depFile = new File(dependency);
            /* Convert dependency path to absolute path for classpath */
            joiner.add(depFile.getAbsolutePath());
        }
        return joiner.toString();
    }

    //<editor-fold desc="JavaBeans getters/setters">

    public String getCorrectSolution() {
        return correctSolution;
    }

    public void setCorrectSolution(String correctSolution) {
        this.correctSolution = correctSolution;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public Conformance.ConformanceOptions getConformance() {
        return conformance;
    }

    public void setConformance(Conformance.ConformanceOptions conformance) {
        this.conformance = conformance;
    }

    public Functionality.FunctionalityOptions getFunctionality() {
        return functionality;
    }

    public void setFunctionality(Functionality.FunctionalityOptions functionality) {
        this.functionality = functionality;
    }

    public JUnit.JUnitOptions getJunit() {
        return junit;
    }

    public void setJunit(JUnit.JUnitOptions junit) {
        this.junit = junit;
    }

    public Checkstyle.CheckstyleOptions getCheckstyle() {
        return this.checkstyle;
    }

    public void setCheckstyle(Checkstyle.CheckstyleOptions checkstyle) {
        this.checkstyle = checkstyle;
    }

    //</editor-fold>
}
