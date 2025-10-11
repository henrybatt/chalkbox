package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.source.Solution;
import chalkbox.stages.StageException;
import chalkbox.source.Submission;
import com.google.common.flogger.FluentLogger;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "conformance",
        description = "Runs conformance over the input project ")
public class Conformance implements Runnable {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        Path configFile = Paths.get(shared.configFile);
        var config = ConfigurationLoader.load(configFile, Config::new);

        //todo(mh): Config this
        var solution = new Solution("./test/resources/csse2002/solutions/correct", "");
        var submission = new Submission(shared.submissionPath, "");

        var stage = config.toConformance();
        try {
            var result = stage.run(submission, solution);
            logger.atInfo().log("Conformance Run %s", result.overview().getOutput());
        } catch (StageException e) {
            logger.atSevere().log(e.toString());
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
