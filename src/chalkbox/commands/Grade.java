package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.Result;
import chalkbox.stages.Stage;
import chalkbox.stages.StageException;
import chalkbox.stages.StageResult;
import chalkbox.stages.ai.Ai;
import chalkbox.stages.header.Header;
import com.google.common.flogger.FluentLogger;
import de.bsommerfeld.jshepherd.core.ConfigurationLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "grade",
        description = "Runs a sequence of stages and generates a Gradescope submission result")
public class Grade implements Runnable {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @CommandLine.Option(names = { "--stages" }, required = true, description = "Comma seperated stages to run")
    public String stages;

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        Path configFile = Paths.get(shared.configFile);
        var config = ConfigurationLoader.load(configFile, Config::new);

        logger.atInfo().log("Running the following stages: " + String.join(" ,", stages));

        //todo(mh): Config this
        var solution = new Solution("./test/resources/csse2002/solutions/correct",
                "./test/resources/csse2002/lib/junit-4.12.jar");
        var submission = new Submission(shared.submissionPath, "./test/resources/csse2002/lib/junit-4.12.jar");

        // for each stage in the config
        var stages = this.stages.split(",");
        for (var name : stages) {
            var stage = getStage(name, config);
            if (stage == null) {
                logger.atWarning().log("Unable to find stage for " + name);
                continue;
            }
            StageResult result = null;
            try {
                switch (stage.getType()) {
                    case SUBMISSION_ONLY -> result = stage.run(submission);
                    case SUBMISSION_AND_SOLUTION -> result = stage.run(submission, solution);
                }
            } catch (StageException e) {
                result = StageResult.fromOverview(new Result(stage.getName()));
            }

            logger.atInfo().log(stage.getName());
            if (result == null) {
                continue;
            }
            logger.atInfo().log(result.overview().getOutput());
        }
    }

    private Stage getStage(String name, Config config) {
        return switch (name) {
            case "header" -> new Header();
            case "ai" -> new Ai();
            case "codestyle" -> config.toCodestyle();
            case "conformance" -> config.toConformance();
            case "functionality" -> config.toFunctionality();
            case "mutation" -> config.toMutation();
            default -> null;
        };
    }
}
