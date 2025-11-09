package chalkbox.commands;

import chalkbox.config.Config;
import chalkbox.stages.*;
import com.google.common.flogger.FluentLogger;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "grade",
    description = "Runs a sequence of stages and generates a Gradescope submission result"
)
public class Grade implements Runnable {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @CommandLine.Option(
        names = { "--stages" },
        required = true,
        description = "Comma separated stages to run"
    )
    public String stages;

    @Mixin
    Shared shared = new Shared();

    private Map<String, Class<StageProducer>> registeredClasses =
        getRegisteredStages();

    @Override
    public void run() {
        var gradescope = new GradescopeResult();
        Path configFile = Paths.get(shared.configFile);
        var config = new Config(configFile);

        logger
            .atInfo()
            .log("Running the following stages: " + String.join(" ,", stages));

        var solution = config.toSolution();
        var submission = config.toSubmission();

        // for each stage in the config
        var stages = this.stages.split(",");
        for (var name : stages) {
            var registeredClass = registeredClasses.get(name.trim());
            if (registeredClass == null) {
                logger.atWarning().log("Unable to find stage for " + name);
                continue;
            }

            Stage stage = null;
            StageProducer producer = null;
            try {
                producer =
                registeredClass.getDeclaredConstructor().newInstance();
                stage = producer.build(config);
            } catch (Exception e) {
                throw new StageException(e);
            }
            logger.atInfo().log(stage.getName());

            StageResult result = null;
            try {
                switch (stage.getType()) {
                    case SUBMISSION_ONLY -> result = stage.run(submission);
                    case SUBMISSION_AND_SOLUTION -> result =
                    stage.run(submission, solution);
                    default -> throw new StageException(
                        "Unsupported stage type."
                    );
                }
            } catch (StageException e) {
                logger
                    .atSevere()
                    .withCause(e)
                    .log("Unable to run stage " + stage.getName());
                Result details = new Result(stage.getName());
                details.appendOutput(
                    "Unable to run " +
                    stage.getName() +
                    " stage while grading. The following error occurred.\n"
                );
                details.appendOutput(
                    "Please consult course staff if you need help interpreting this error.\n"
                );
                details.appendOutput(e.toString());
                result = StageResult.fromOverview(details);
            } catch (Throwable e) {
                logger
                    .atSevere()
                    .withCause(e)
                    .log("Panicked while running stage" + stage.getName());
                Result details = new Result(stage.getName());
                details.appendOutput(
                    "Panicked while running " +
                    stage.getName() +
                    " stage while grading. The following error occurred.\n"
                );
                details.appendOutput(
                    "Please consult course staff if this error has occurred.\n"
                );
                details.appendOutput(e.toString());
                result = StageResult.fromOverview(details);
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

    private Map<String, Class<StageProducer>> getRegisteredStages() {
        var reflections = new Reflections("chalkbox.stages");
        var annotatedClasses = reflections.getTypesAnnotatedWith(
            RegisterStage.class
        );

        return annotatedClasses
            .stream()
            .filter(StageProducer.class::isAssignableFrom)
            .collect(
                Collectors.toMap(
                    clazz -> {
                        if (
                            clazz
                                .getAnnotation(RegisterStage.class)
                                .value()
                                .isEmpty()
                        ) {
                            return clazz.getSimpleName().toLowerCase();
                        }
                        return clazz.getAnnotation(RegisterStage.class).value();
                    },
                    clazz -> (Class<StageProducer>) clazz
                )
            );
    }
}
