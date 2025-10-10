package chalkbox.config;

import chalkbox.stages.functionality.Functionality;
import chalkbox.stages.codestyle.CodeStyle;
import chalkbox.stages.conformance.Conformance;
import chalkbox.stages.mutation.Mutation;
import de.bsommerfeld.jshepherd.annotation.Comment;
import de.bsommerfeld.jshepherd.annotation.Key;
import de.bsommerfeld.jshepherd.annotation.PostInject;
import de.bsommerfeld.jshepherd.core.ConfigurablePojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Comment("My Application Configuration")
public class Config extends ConfigurablePojo<Config> {

    @Key("debug-mode")
    @Comment("Enable debug logging")
    public boolean debugMode = false;

    @Key("codestyle.weighting")
    public int codestyleWeighting;

    @Key("codestyle.penaltyPerInfraction")
    public float codestylePenaltyPerInfraction;

    @Key("codestyle.excluded")
    public List<String> codestyleExcluded = new ArrayList<>();

    @Key("compilation.classPath")
    public List<String> compilationClassPath = new ArrayList<>();

    @Key("conformance.path")
    public String conformancePath;

    public Config() {
    }

    @PostInject
    private void validateConfigValues() {
    }

    public CodeStyle toCodestyle() {
        return new CodeStyle(this.codestyleWeighting, this.codestylePenaltyPerInfraction, this.codestyleExcluded);
    }

    public Conformance toConformance() {
        return new Conformance(new ArrayList<>());
    }

    public Functionality toFunctionality() {
        return new Functionality(38);
    }

    public Mutation toMutation() {
        return new Mutation(38);
    }
}