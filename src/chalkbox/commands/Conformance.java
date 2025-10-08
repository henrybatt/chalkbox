package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.stages.StageException;
import chalkbox.submission.Submission;
import com.google.common.flogger.FluentLogger;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;
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

        var submission = new Submission(shared.submissionPath);

        // always need to compile first
        var compileStage = config.toCompilation();
        try {
            compileStage.run(submission);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var stage = config.toConformance();
        try {
            var result = stage.run(submission);
            logger.atInfo().log("CodeStyle Run %s", result.getComment());
        } catch (StageException e) {
            logger.atSevere().log(e.toString());
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
