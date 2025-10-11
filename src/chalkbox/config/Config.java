package chalkbox.config;

import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.functionality.Functionality;
import chalkbox.stages.codestyle.CodeStyle;
import chalkbox.stages.conformance.Conformance;
import chalkbox.stages.mutation.Mutation;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.EnvironmentConfigSourceBuilder;
import org.github.gestalt.config.source.FileConfigSourceBuilder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private Gestalt gestalt;

    public Config(Path path) throws ConfigException {
        GestaltBuilder builder = new GestaltBuilder();
        try {
            this.gestalt = builder
                    .addSource(FileConfigSourceBuilder.builder().setPath(path).build())
                    .build();
        } catch (GestaltException e) {
            throw new ConfigException("unable to setup discovery for configuration: " + e);
        }

        try {
            gestalt.loadConfigs();
        } catch (GestaltException e) {
            throw new ConfigException("unable to load config: " + e);
        }
    }

    public CodeStyle toCodestyle() throws ConfigException {
        try {
            return new CodeStyle(
                    gestalt.getConfig("codestyle.weighting", Double.class),
                    gestalt.getConfig("codestyle.penalty", Float.class),
                    gestalt.getConfig("codestyle.excluded", new TypeCapture<List<String>>() {})
            );
        } catch (GestaltException e) {
            throw new ConfigException(e.toString());
        }
    }

    public Conformance toConformance() {
        return new Conformance(new ArrayList<>());
    }

    public Functionality toFunctionality() {
        return new Functionality(38);
    }

    public Mutation toMutation() throws ConfigException {
        try {
            return new Mutation(gestalt.getConfig("mutation.weighting", Double.class));
        } catch (GestaltException e) {
            throw new ConfigException(e.toString());
        }
    }

    public Submission toSubmission() throws ConfigException {
        try {
            return new Submission(
                    gestalt.getConfig("submission.path", String.class),
                    gestalt.getConfig("submission.classPath", new TypeCapture<List<String>>() {})
            );
        } catch (GestaltException e) {
            throw new RuntimeException(e);
        }
    }

    public Solution toSolution() throws ConfigException {
        try {
            return new Solution(
                    gestalt.getConfig("submission.path", String.class),
                    gestalt.getConfig("submission.classPath", new TypeCapture<List<String>>() {})
            );
        } catch (GestaltException e) {
            throw new RuntimeException(e);
        }
    }
}