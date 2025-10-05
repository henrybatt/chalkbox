package chalkbox.stages;

public class StageResult implements Result {

    private int maxScore;
    private int score;
    private String comments = "";

    @Override
    public String getName() {
        return "Automated Style";
    }

    @Override
    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int score) {
        this.maxScore = score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int getScore() {
        return score;
    }

    public void appendComment(String input) {
        this.comments += input + System.lineSeparator();
    }

    public void setComment(String comments) {
        this.comments = comments;
    }

    @Override
    public String getComment() {
        return this.comments;
    }
}
