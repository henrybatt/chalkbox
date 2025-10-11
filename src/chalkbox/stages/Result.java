package chalkbox.stages;

public class Result {

    private final String name;
    private final String nameFormat;
    private double score;
    private double maxScore;
    private Status status;
    private String output;
    private String outputFormat;
    private Visibility visibility;

    public Result(String name) {
        this(name, "text");
    }

    public Result(String name, String nameFormat) {
        this.name = name;
        this.nameFormat = nameFormat;
        this.status = Status.FAILED;
        this.output = "";
        this.outputFormat = "md";
        this.visibility = Visibility.VISIBLE;
    }

    public String getName() {
        return name;
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public Result setScore(double score) {
        this.score = score;
        return this;
    }

    public double getScore() {
        return this.score;
    }

    public Result setMaxScore(double maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    public double getMaxScore() {
        return this.maxScore;
    }

    public Result setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Result setOutputFormat(String format) {
        this.outputFormat = format;
        return this;
    }

    public Result appendOutput(String input) {
        this.output += input;
        return this;
    }

    public String getOutput() {
        return this.output;
    }

    public Result setVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }
}
