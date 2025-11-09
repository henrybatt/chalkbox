package chalkbox.api.common.java;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * Utility class for compiling Java source code.
 */
public class Compiler {

    /**
     * Compile java source files into java byte code files.
     *
     * If the output path doesn't exist it will attempt to be created.
     *
     * @param files The source java files to compile.
     * @param classPath The classpath to compile with.
     * @param outputPath The path of the folder to output the java byte code files.
     * @param output A string writer for the output from compiling the source files.
     *
     * @return true iff the files were compiled successfully.
     */
    public static boolean compile(
        Iterable<? extends JavaFileObject> files,
        String classPath,
        String outputPath,
        StringWriter output
    ) {
        /* Try to create the output path directory */
        File outFile = new File(outputPath);
        if (!outFile.exists()) {
            if (!outFile.mkdirs()) {
                output.write("Unable to create output directory - See tutor");
                return false;
            }
        }

        List<String> options = new ArrayList<>();
        options.add("-processor");
        options.add(
            "com.github.therapi.runtimejavadoc.scribe.JavadocAnnotationProcessor"
        );
        options.add("-cp");
        options.add(classPath);
        options.add("-d");
        options.add(outputPath);
        options.add("-Xlint:-options");

        return compile(files, output, options);
    }

    /**
     * Compile java source files into java byte code files.
     *
     * @param files The source java files to compile.
     * @param output A string writer for the output from compiling the source files.
     * @param options Command line options for the compilation process.
     *
     * @return true iff the files were compiled successfully.
     */
    public static boolean compile(
        Iterable<? extends JavaFileObject> files,
        StringWriter output,
        List<String> options
    ) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        boolean success;
        try {
            success =
            compiler.getTask(output, null, null, options, null, files).call();
        } catch (IllegalStateException e) {
            output.write("Empty submission");
            return false;
        }

        return success;
    }
}
