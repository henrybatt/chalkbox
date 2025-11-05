package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.stages.StageException;
import com.google.common.flogger.FluentLogger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "bugfixes",
        description = "Runs bug fixing marking")
public class BugFixes implements Runnable {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        Path configFile = Paths.get(shared.configFile);
        var config = new Config(configFile);

        var solution = config.toSolution();
        var submission = config.toSubmission();
        var stage = config.toBugFixes();
        try {
            var result = stage.run(submission, solution);
            logger.atInfo().log("Bug Fixes Run");
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
