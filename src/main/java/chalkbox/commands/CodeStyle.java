package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.stages.StageException;
import chalkbox.source.Submission;
import com.google.common.flogger.FluentLogger;
import org.github.gestalt.config.exceptions.GestaltException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "codestyle",
        description = "Runs the codestyle over the project")
public class CodeStyle implements Runnable {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        Path configFile = Paths.get(shared.configFile);
        Config config = new Config(configFile);

        var submission = config.toSubmission();
        var stage = config.toCodestyle();
        //todo(mh): Add handling here for overriding checkstyle config
        try {
            var result = stage.run(submission);
            logger.atInfo().log("CodeStyle Run %s", result.overview().getOutput());
        } catch (StageException e) {
            logger.atSevere().log(e.toString());
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
