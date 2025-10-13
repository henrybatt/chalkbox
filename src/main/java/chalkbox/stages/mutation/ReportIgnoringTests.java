package chalkbox.stages.mutation;

import org.pitest.mutationtest.config.ReportOptions;

import java.util.function.Predicate;

public class ReportIgnoringTests extends ReportOptions {
    @Override
    public Predicate<String> getTargetClassesFilter() {
        Predicate<String> existingFilter = super.getTargetClassesFilter();
        return existingFilter.and(Predicate.not((targetName) -> targetName.endsWith("Test")));
    }
}
