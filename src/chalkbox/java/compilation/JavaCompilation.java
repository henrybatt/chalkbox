package chalkbox.java.compilation;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import org.json.simple.JSONArray;

import javax.tools.JavaFileObject;
import java.io.StringWriter;

/**
 * Process to compile all of the .java source files in a submission.
 *
 * <p>A compiling submission may produce the following output, where ... stands
 * for any output warnings when compiling:
 * <pre>
 * "compilation": {
 *     "compiles": true,
 *     "output": "..."
 * }
 * </pre>
 *
 * <p>A non-compiling submission may produce the following output, where ... stands
 * for the compilation errors:
 * <pre>
 * "compilation": {
 *     "compiles": false,
 *     "output": "..."
 * }
 * </pre>
 *
 * <p>A submission without any .java files will have compilation.compiles set
 * to false and compilation.output set to "Empty submission"
 */
public class JavaCompilation {

    /** Class path to use to compile submissions */
    private String classPath;

    private static final String SUCCESS_MSG = "Submission successfully compiled";
    private static final String FAILURE_MSG = "Submission did not compile";

    public JavaCompilation(String classPath) {
        this.classPath = classPath;
    }

    public Collection compile(Collection submission) {
        Bundle working = submission.getWorking();
        Data results = submission.getResults();

        /* Create a visible test result for compilation status and output */
        JSONArray testResults = (JSONArray) results.get("tests");
        Data compilationResult = new Data();
        compilationResult.set("name", "Compilation");
        compilationResult.set("output", "");
        testResults.add(compilationResult);

        results.set("extra_data.compilation.compiles", false);

        /* Attempt to create a new output directory */
        if (!working.makeDir("bin")) {
            System.err.println("Couldn't create output directory");
            compilationResult.set("output", FAILURE_MSG
                    + "\nCouldn't create output directory - see tutor");
            return submission;
        }

        Iterable<? extends JavaFileObject> sourceFiles = Compiler.getSourceFiles(
                submission.getSource());
        if (sourceFiles == null) {
            System.err.println("Couldn't load source files");
            compilationResult.set("output", FAILURE_MSG
                            + "\nError loading source files - see tutor");
            return submission;
        }

        StringWriter output = new StringWriter();
        String classPath = submission.getSource().getUnmaskedPath()
                + System.getProperty("path.separator") + this.classPath;

        boolean success = Compiler.compile(sourceFiles, classPath,
                working.getUnmaskedPath("bin"), output);

        results.set("extra_data.compilation.compiles", success);
        if (success) {
            compilationResult.set("output", SUCCESS_MSG + "\n" + output.toString());
        } else {
            compilationResult.set("output", FAILURE_MSG + "\n" + output.toString());
        }

        working.refresh();
        return submission;
    }
}
