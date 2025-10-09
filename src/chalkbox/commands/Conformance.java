package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.source.Solution;
import chalkbox.stages.StageException;
import chalkbox.source.Submission;
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

        var solution = new Solution("/home/millie/Documents/projects/chalkbox/test/resources/csse2002/solutions/correct", "");
        try {
            var compiled = solution.compile();
            if (!compiled) {
                logger.atSevere().log("Could not compile the solution");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        chalkbox.stages.conformance.Conformance stage = null;
        try {
            stage = config.toConformance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var submission = new Submission(shared.submissionPath, "");
        try {
            submission.compile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            var result = stage.run(submission, solution);
            logger.atInfo().log("Conformance Run %s", result.getComment());
        } catch (StageException e) {
            logger.atSevere().log(e.toString());
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
