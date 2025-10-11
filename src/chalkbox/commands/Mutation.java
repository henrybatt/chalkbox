package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.StageException;
import com.google.common.flogger.FluentLogger;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "mutation",
        description = "Runs mutation over the input project")
public class Mutation implements Runnable {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        Path configFile = Paths.get(shared.configFile);
        var config = ConfigurationLoader.load(configFile, Config::new);

        //todo(mh): Config this
        var submission = new Submission(shared.submissionPath, "./test/resources/csse2002/lib/junit-4.12.jar");

        var stage = config.toMutation();
        try {
            var result = stage.run(submission);
            logger.atInfo().log("Mutation Run");
            logger.atInfo().log(result.overview().getOutput());
            for (var inner : result.results()) {
                logger.atInfo().log(inner.getOutput());
            }
        } catch (StageException e) {
            logger.atSevere().log(e.toString());
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
