package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.java.checkstyle.Checkstyle;
import chalkbox.java.compilation.JavaCompilation;
import chalkbox.java.conformance.Conformance;
import chalkbox.java.junit.JUnit;
import chalkbox.java.test.JavaTest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

public class CSSE2002Engine extends Engine {

    public static class CheckstyleOptions {
        private String config;
        private String jar;
        private List<String> excluded;
        private double violationPenalty = 1;

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
    }

    private List<String> dependencies;
    private List<String> resources;
    private String testDirectory;
    private String correctSolution;
    private String expectedStructure;
    private CheckstyleOptions checkstyle;
    private String faultySolutions;
    private List<String> assessableTestClasses;

    @Override
    public boolean configIsValid() {
        if (!super.configIsValid()) {
            return false;
        }

        /* "assessableTestClasses" and "faultySolutions" are needed for JUnit */
        // TODO refactor these keys into a single "junit" dictionary
        if (this.getStages().containsKey("junit")) {
            if (this.assessableTestClasses == null
                    || this.assessableTestClasses.isEmpty()) {
                return false;
            }
            if (this.faultySolutions == null
                    || this.faultySolutions.isEmpty()) {
                return false;
            }
        }

        /* "checkstyle" is needed for automatic style marking */
        if (this.getStages().containsKey("autostyle")) {
            if (this.checkstyle == null) {
                return false;
            }
        }

        /* "expectedStructure" is needed for conformance checking */
        if (this.getStages().containsKey("conformance")) {
            if (this.expectedStructure == null
                    || this.expectedStructure.isEmpty()) {
                return false;
            }
        }

        return true;
    }

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
                    dependenciesToClasspath(this.dependencies),
                    getWeighting("conformance"));
            submission = conformance.run(submission);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        JavaTest test = new JavaTest(this.correctSolution, this.testDirectory,
                dependenciesToClasspath(this.dependencies),
                getWeighting("functionality"));
        submission = test.run(submission);

        // Test submitted JUnit classes
        if (this.getStages().containsKey("junit")) {
            JUnit jUnit = new JUnit(this.correctSolution, this.faultySolutions,
                    this.assessableTestClasses,
                    dependenciesToClasspath(this.dependencies),
                    getWeighting("junit"));
            submission = jUnit.run(submission);
        }

        if (this.getStages().containsKey("autostyle")) {
            Checkstyle checkstyle = new Checkstyle(this.checkstyle.getJar(),
                    this.checkstyle.getConfig(), this.checkstyle.getExcluded(),
                    getWeighting("autostyle"),
                    this.checkstyle.violationPenalty);
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
