package chalkbox.stages;

public abstract class BaseStage implements Stage, StageProducer {

    private final String name;

    public BaseStage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
