package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.StageException;
import com.google.common.flogger.FluentLogger;
import org.github.gestalt.config.exceptions.GestaltException;
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
        var config = new Config(configFile);

        var solution = config.toSolution();
        var submission = config.toSubmission();
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
