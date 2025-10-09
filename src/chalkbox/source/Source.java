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
    private boolean canCompile = false;
    private boolean hasCompiled = false;
    private String classPath;
    private String name;

    public Source(String name, String root, String classPath) {
        this.name = name;
        var path = Paths.get(root);
        this.basePath = path.toAbsolutePath().toString();
        this.classPath = classPath;
    }

    public void setCanCompile(boolean canCompile) {
        this.canCompile = canCompile;
    }

    public boolean compiles() {
        if (!hasCompiled) {
            throw new SourceNotCompiled("A compilation for this source has not been attempted.");
        }
        return canCompile;
    }

    public boolean compile() throws IOException {
        var build = new File(getSrcBuildPath());

        // Check if the folder exists
        if (!build.exists()) {
            if(build.mkdirs()) {
                logger.atInfo().log("build filepath created %s", build.getAbsolutePath());
            } else {
                logger.atSevere().log("unable to make build path %s", build.getAbsolutePath());
                throw new StageException("Unable to make build path");
            }
        }

        Iterable<? extends JavaFileObject> sourceFiles = getSrcJavaFiles();
        if (sourceFiles == null) {
            throw new StageException("Couldn't load source files");
        }

        StringWriter output = new StringWriter();
        String classPath = getSrcFolder() + File.pathSeparator + this.classPath;

        var compiled = Compiler.compile(sourceFiles, classPath, build.getAbsolutePath(), output);
        this.markCompiled(compiled);
        return compiled;
    }

    private void markCompiled(boolean compiled) {
        this.canCompile = compiled;
        this.hasCompiled = compiled;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public String getSrcBuildPath() {
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
