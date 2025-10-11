package chalkbox.stages.mutation;

import chalkbox.source.Submission;
import chalkbox.stages.Result;
import chalkbox.stages.Status;
import org.pitest.mutationtest.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class MutationListener implements MutationResultListenerFactory {
    private final List<Result> mutationResults = new ArrayList<>();

    public List<Result> getResults() {
        return mutationResults;
    }

    @Override
    public MutationResultListener getListener(Properties props, ListenerArguments args) {
        return new MutationResultListener() {
            @Override
            public void runStart() {

            }

            @Override
            public void handleMutationResult(ClassMutationResults results) {
                Result testResult = new Result("Mutation: " + results.getMutatedClass().asJavaName());

                for (MutationResult mutation : results.getMutations()) {
                    String headline = "Mutated line " + mutation.getDetails().getLineNumber() + " in " + mutation.getDetails().getFilename();
                    String howChange = "Mutation: `" + mutation.getDetails().getDescription() + "`";
                    String result;
                    boolean passes = false;
                    switch (mutation.getStatus()) {
                        case NO_COVERAGE -> {
                            result = "❌ No test covers the mutated line";
                        }
                        case SURVIVED -> {
                            result = "❌ No test detected the mutation";
                        }
                        case KILLED -> {
                            Optional<String> killing = mutation.getKillingTest();
                            result = killing.map(s -> "✅ " + s + " detects this mutation").orElse("✅ detected this mutation");
                            passes = true;
                        }
                        default -> {
                            result = "❌ Unhandled mutation status: " + mutation.getStatus() + " please report this to course staff.";
                        }
                    }

                    testResult.appendOutput(String.join(System.lineSeparator(), headline, howChange, result) + System.lineSeparator() + System.lineSeparator());
                    testResult.setStatus(passes ? Status.PASSED : Status.FAILED);
                }

                mutationResults.add(testResult);
            }

            @Override
            public void runEnd() {

            }
        };
    }

    @Override
    public String name() {
        return "Chalkbox";
    }

    @Override
    public String description() {
        return "Chalkbox Mutation Listener";
    }
}
