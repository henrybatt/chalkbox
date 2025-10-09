package chalkbox.source;

import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.FileSourceFile;
import chalkbox.stages.StageException;
import chalkbox.stages.conformance.SourceLoader;
import com.google.common.flogger.FluentLogger;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Source {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private final String basePath;
    private String classPath;
    private String name;
    private CompilationResult srcCompilation;
    private CompilationResult testCompilation;

    public Source(String name, String root, String classPath) {
        this.name = name;
        var path = Paths.get(root);
        this.basePath = path.toAbsolutePath().toString();
        this.classPath = classPath;
    }

    public String getClassPath() {
        return this.classPath;
    }

    public CompilationResult compileSrc() throws IOException {
        if (srcCompilation != null) {
            return srcCompilation;
        }
        return compile(getSrcBuildPath(), getSrcJavaFiles(), "");
    }

    public CompilationResult compileTest() throws IOException {
        if (testCompilation != null) {
            return testCompilation;
        }
        return compile(getTestBuildPath(), getTestJavaFiles(), getSrcBuildPath());
    }

    private CompilationResult compile(String destination, List<FileSourceFile> sourceFiles, String additionalClasspath) throws IOException {
        if (sourceFiles == null) {
            throw new StageException("Couldn't load source files");
        }

        var build = new File(destination);

        // Check if the folder exists
        if (!build.exists()) {
            if(build.mkdirs()) {
                logger.atInfo().log("build filepath created %s", build.getAbsolutePath());
            } else {
                logger.atSevere().log("unable to make build path %s", build.getAbsolutePath());
                throw new StageException("Unable to make build path");
            }
        }

        var classPath = this.classPath;
        if (!additionalClasspath.isEmpty()) {
            classPath += File.pathSeparator + additionalClasspath;
        }

        var output = new StringWriter();
        var success = Compiler.compile(sourceFiles, classPath, build.getAbsolutePath(), output);

        return new CompilationResult(success, output.toString());
    }

    public String getBasePath() {
        return basePath;
    }

    public String getSrcBuildPath() {
        return basePath + "/build/classes/";
    }

    public String getSrcFolder() {
        return basePath + "/src";
    }

    public String getTestBuildPath() {
        return basePath + "/build/test/";
    }

    public String getTestFolder() {
        return basePath + "/test";
    }

    public List<FileSourceFile> getSrcJavaFiles() throws IOException {
        return getFilesWithExtension(getSrcFolder(), ".java");
    }

    public List<FileSourceFile> getTestJavaFiles() throws IOException {
        return getFilesWithExtension(getTestFolder(), ".java");
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

    public SourceLoader getSrcLoader() throws IOException {
        return getSourceLoader(getSrcBuildPath());
    }

    private SourceLoader getSourceLoader(String directory) throws IOException {
        URL[] urls = Arrays.stream(classPath.split(":"))
                .filter(e -> !e.isEmpty())
                .map(e -> {
                    try {
                        return new URI("file://" + e).toURL();
                    } catch (MalformedURLException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                }).toArray(URL[]::new);
        var loader = new URLClassLoader(urls);
        return new SourceLoader(directory, loader);
    }
}
