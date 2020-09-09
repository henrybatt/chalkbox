package chalkbox.engines;

import chalkbox.api.collections.Collection;
import chalkbox.collectors.GradescopeCollector;
import chalkbox.java.compilation.JavaCompilation;
import chalkbox.java.test.JavaTest;
import chalkbox.output.GradescopeOutput;

import java.util.List;
import java.util.StringJoiner;

public class CSSE2002Engine extends Engine {

    private List<String> dependencies;
    private List<String> resources;
    private String linterConfig;
    private String testDirectory;
    private String correctSolution;
    // TODO: add faultySolutions and assessableTestClasses

    @Override
    public void run() {
        super.run();
        System.out.println("Running CSSE2002 engine");

        // TODO remove collection and output into Engine?
        GradescopeCollector collector = new GradescopeCollector();
        Collection submission = collector.collect();

        JavaCompilation compilation = new JavaCompilation(
                dependenciesToClasspath(this.dependencies));
        submission = compilation.compile(submission);

        JavaTest test = new JavaTest(this.correctSolution, this.testDirectory,
                dependenciesToClasspath(this.dependencies));
        test.compileTests(null); // TODO remove unused param
        submission = test.runTests(submission);

        GradescopeOutput output = new GradescopeOutput();
        output.output(null, submission); // TODO remove unused param
    }

    private String dependenciesToClasspath(List<String> dependencies) {
        StringJoiner joiner = new StringJoiner(":");
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
}
