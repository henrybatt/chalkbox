package chalkbox.stages.mutation;

import java.util.function.Predicate;
import org.pitest.mutationtest.config.ReportOptions;

public class ReportIgnoringTests extends ReportOptions {

    @Override
    public Predicate<String> getTargetClassesFilter() {
        Predicate<String> existingFilter = super.getTargetClassesFilter();
        return existingFilter.and(
            Predicate.not(targetName -> targetName.endsWith("Test"))
        );
    }
}
