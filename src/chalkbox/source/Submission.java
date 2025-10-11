package chalkbox.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Submission extends Source {

    public Submission(String baseDirectory, String classPath) {
        super("submission", baseDirectory, classPath);
    }

    public String getAiDeclaration() {
        try {
            return Files.readString(Path.of(getBasePath() + "/ai/" + "README.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

}
