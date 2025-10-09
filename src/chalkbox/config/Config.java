package chalkbox.config;

import chalkbox.stages.codestyle.CodeStyle;
import chalkbox.stages.compilation.Compilation;
import chalkbox.stages.conformance.Conformance;
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

    public Compilation toCompilation() {
        return new Compilation("");
    }

    public Conformance toConformance() throws IOException {
        return new Conformance(new ArrayList<>());
    }
}