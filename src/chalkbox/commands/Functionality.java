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

@Command(name = "functionality",
        description = "Runs functionality over the input project")
public class Functionality implements Runnable {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        Path configFile = Paths.get(shared.configFile);
        var config = ConfigurationLoader.load(configFile, Config::new);

        //todo(mh): Config this
        var solution = new Solution("./test/resources/csse2002/solutions/correct",
                "./test/resources/csse2002/lib/junit-4.12.jar");
        var submission = new Submission(shared.submissionPath, "./test/resources/csse2002/lib/junit-4.12.jar");

        var stage = config.toFunctionality();
        try {
            var result = stage.run(submission, solution);
            logger.atInfo().log("Functionality Run");
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
