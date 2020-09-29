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

public class JavaEngine extends Engine {

    private String correctSolution;
    private List<String> dependencies;
    private List<String> resources; // TODO

    private Conformance.ConformanceOptions conformance;
    private Functionality.FunctionalityOptions functionality;
    private JUnit.JUnitOptions junit;
    private Checkstyle.CheckstyleOptions checkstyle;

    @Override
    public boolean configIsValid() {
        if (!super.configIsValid()) {
            return false;
        }

        /* All stages are optional */

        if (this.conformance != null && !this.conformance.isValid()) {
            return false;
        }

        if (this.functionality != null && !this.functionality.isValid()) {
            return false;
        }

        if (this.junit != null && !this.junit.isValid()) {
            return false;
        }

        if (this.checkstyle != null && !this.checkstyle.isValid()) {
            return false;
        }

        return true;
    }

    @Override
    public void run() {
        System.out.println("Running CSSE2002 engine");

        Collection submission = super.collect();

        /* Convert list of dependencies to a single classpath string */
        String classPath = dependenciesToClasspath(this.dependencies);

        JavaCompilation compilation = new JavaCompilation(classPath);
        submission = compilation.compile(submission);

        if (this.conformance != null) {
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

        if (this.functionality != null) {
            this.functionality.setCorrectSolution(correctSolution);
            this.functionality.setClassPath(classPath);
            Functionality test = new Functionality(this.functionality);
            submission = test.run(submission);
        }

        if (this.junit != null) {
            this.junit.setCorrectSolution(correctSolution);
            this.junit.setClassPath(classPath);
            JUnit jUnit = new JUnit(this.junit);
            submission = jUnit.run(submission);
        }

        if (this.checkstyle != null) {
            Checkstyle checkstyle = new Checkstyle(this.checkstyle);
            submission = checkstyle.run(submission);
        }

        super.output(submission);
    }

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

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
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
