package chalkbox.submission;

import chalkbox.api.files.FileSourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Submission {

    private final String basePath;
    private boolean canCompile = false;

    public Submission(String root) {
        var path = Paths.get(root);
        this.basePath = path.toAbsolutePath().toString();
    }

    public void setCanCompile(boolean canCompile) {
        this.canCompile = canCompile;
    }

    public boolean compiles() {
        return canCompile;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public String getBuildPath() {
        return this.basePath + "/build/classes/";
    }

    public String getSrcFolder() {
        return this.basePath + "/src";
    }

    public List<FileSourceFile> getSrcJavaFiles() throws IOException {
        return getFilesWithExtension(getSrcFolder(), ".java");
    }

    private List<FileSourceFile> getFilesWithExtension(String root, String extension) throws IOException {
        var start = Paths.get(root);

        try (Stream<Path> stream = Files.walk(start)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(extension))
                    .map(path -> new FileSourceFile(path.toString(), path.toFile()))
                    .collect(Collectors.toList());
        }
    }
}
