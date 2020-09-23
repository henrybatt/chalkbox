package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.java.checkstyle.Checkstyle;
import chalkbox.java.compilation.JavaCompilation;
import chalkbox.java.conformance.Conformance;
import chalkbox.java.junit.JUnit;
import chalkbox.java.test.JavaTest;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

public class CSSE2002Engine extends Engine {

    public static class CheckstyleOptions {
        private String config;
        private String jar;
        private List<String> excluded;

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
    }

    private List<String> dependencies;
    private List<String> resources;
    private String linterConfig;
    private String testDirectory;
    private String correctSolution;
    private String expectedStructure;
    private CheckstyleOptions checkstyle;
    private String faultySolutions;
    private List<String> assessableTestClasses;

    @Override
    public void run() {
        System.out.println("Running CSSE2002 engine");

        Collection submission = super.collect();

        JavaCompilation compilation = new JavaCompilation(
                dependenciesToClasspath(this.dependencies));
        submission = compilation.compile(submission);

        try {
            Conformance conformance = new Conformance(this.correctSolution,
                    this.expectedStructure,
                    dependenciesToClasspath(this.dependencies));
            submission = conformance.run(submission);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        JavaTest test = new JavaTest(this.correctSolution, this.testDirectory,
                dependenciesToClasspath(this.dependencies));
        test.compileTests(null); // TODO remove unused param; move to constructor
        submission = test.runTests(submission);

        if (checkstyle != null) {
            Checkstyle checkstyle = new Checkstyle(this.checkstyle.getJar(),
                    this.checkstyle.getConfig(), this.checkstyle.getExcluded());
            submission = checkstyle.run(submission);
        }

        // Test submitted JUnit classes
        if (faultySolutions != null) {
            JUnit jUnit = new JUnit(this.correctSolution, this.faultySolutions,
                    this.assessableTestClasses,
                    dependenciesToClasspath(this.dependencies));
            submission = jUnit.run(submission);
        }

        super.output(submission);
    }

    private String dependenciesToClasspath(List<String> dependencies) {
        StringJoiner joiner = new StringJoiner(System.getProperty("path.separator"));
        for (String dependency : dependencies) {
            joiner.add(dependency);
        }
        return joiner.toString();
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

    public String getLinterConfig() {
        return linterConfig;
    }

    public void setLinterConfig(String linterConfig) {
        this.linterConfig = linterConfig;
    }

    public String getTestDirectory() {
        return testDirectory;
    }

    public void setTestDirectory(String testDirectory) {
        this.testDirectory = testDirectory;
    }

    public String getCorrectSolution() {
        return correctSolution;
    }

    public void setCorrectSolution(String correctSolution) {
        this.correctSolution = correctSolution;
    }

    public CheckstyleOptions getCheckstyle() {
        return this.checkstyle;
    }

    public void setCheckstyle(CheckstyleOptions checkstyle) {
        this.checkstyle = checkstyle;
    }

    public String getExpectedStructure() {
        return expectedStructure;
    }

    public void setExpectedStructure(String expectedStructure) {
        this.expectedStructure = expectedStructure;
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
}
