package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.stages.*;
import chalkbox.stages.ai.AI;
import chalkbox.stages.header.Header;
import com.google.common.flogger.FluentLogger;
import com.google.gson.GsonBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "grade",
        description = "Runs a sequence of stages and generates a Gradescope submission result")
public class Grade implements Runnable {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @CommandLine.Option(names = { "--stages" }, required = true, description = "Comma separated stages to run")
    public String stages;

    @Mixin Shared shared = new Shared();

    @Override
    public void run() {
        var gradescope = new GradescopeResult();
        Path configFile = Paths.get(shared.configFile);
        var config = new Config(configFile);

        logger.atInfo().log("Running the following stages: " + String.join(" ,", stages));

        var solution = config.toSolution();
        var submission = config.toSubmission();

        // for each stage in the config
        var stages = this.stages.split(",");
        for (var name : stages) {
            var stage = getStage(name, config);
            if (stage == null) {
                logger.atWarning().log("Unable to find stage for " + name);
                continue;
            }
            logger.atInfo().log(stage.getName());

            StageResult result = null;
            try {
                switch (stage.getType()) {
                    case SUBMISSION_ONLY -> result = stage.run(submission);
                    case SUBMISSION_AND_SOLUTION -> result = stage.run(submission, solution);
                }
            } catch (StageException e) {
                logger.atSevere().withCause(e).log("Unable to run stage " + stage.getName());
                result = StageResult.fromOverview(new Result(stage.getName()));
            }

            if (result == null) {
                continue;
            }
            gradescope.add(result);
        }
        var gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            var writer = new BufferedWriter(new FileWriter(shared.outputFile));
            writer.write(gson.toJson(gradescope));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stage getStage(String name, Config config) {
        return switch (name) {
            case "header" -> new Header();
            case "ai" -> config.toAI();
            case "codestyle" -> config.toCodestyle();
            case "conformance" -> config.toConformance();
            case "functionality" -> config.toFunctionality();
            case "mutation" -> config.toMutation();
            case "tlc" -> config.toTLC();
            default -> null;
        };
    }
}
