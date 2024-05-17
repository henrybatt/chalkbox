package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.java.checkstyle.Checkstyle;
import chalkbox.java.compilation.JavaCompilation;
import chalkbox.java.conformance.Conformance;
import chalkbox.java.junit.JUnit;
import chalkbox.java.functionality.Functionality;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

/**
 * ChalkBox engine for Java submissions.
 *
 * Supports several processing "stages", including:
 * <ul>
 * <li>Checking conformance to a public API.</li>
 * <li>Running JUnit tests against the submission.</li>
 * <li>Assessing submitted JUnit tests by running against faulty implementations.</li>
 * <li>Checking adherence to a style guide by running the Checkstyle tool.</li>
 * </ul>
 */
public class JavaEngine extends Engine implements Configuration {

    /**
     * Path to the correct implementation.
     */
    private String correctSolution;

    /**
     * Paths to libraries required as dependencies when running the engine.
     *
     * For example, JUnit and Hamcrest.
     */
    private List<String> dependencies;

    /* Configuration options for each stage. */
    private Conformance.ConformanceOptions conformance;
    private Functionality.FunctionalityOptions functionality;
    private JUnit.JUnitOptions junit;
    private Checkstyle.CheckstyleOptions checkstyle;

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
