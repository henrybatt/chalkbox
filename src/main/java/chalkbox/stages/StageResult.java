package chalkbox.stages;

import java.util.ArrayList;
import java.util.List;

public record StageResult(Result overview, List<Result> results) {
    public static StageResult fromOverview(Result overview) {
        return new StageResult(overview, new ArrayList<Result>());
    }
}
