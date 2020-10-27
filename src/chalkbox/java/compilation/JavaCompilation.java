package chalkbox.java.compilation;

import chalkbox.api.collections.Bundle;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import chalkbox.api.common.java.Compiler;
import org.json.simple.JSONArray;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Process to compile all of the .java source files in a submission.
 *
 * If the submission compiles, the results JSON will include the following,
 * where ??? stands for any output warnings when compiling:
 * <pre>
 * { ...,
 *   "extra_data": {
 *     "compilation": {
 *       "compiles": true,
 *       "output": "???"
 *     }
 *   }
 * }
 * </pre>
 *
 * If the submission compiles, the results JSON will include the following,
 * where ??? stands for any errors when compiling:
 * <pre>
 * { ...,
 *   "extra_data": {
 *     "compilation": {
 *       "compiles": false,
 *       "output": "???"
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * A submission without any .java files will have
 * <code>extra_data.compilation.compiles</code> set to false and
 * <code>extra_data.compilation.output</code> set to "Empty submission".
 */
public class JavaCompilation {

    /** Message shown in output when submission compiles */
    private static final String FAILURE_MSG = "Submission did not compile";
    /** Message shown in output when submission does not compile */
    private static final String SUCCESS_MSG = "Submission successfully compiled";

    /** Class path to use to compile submissions */
    private String classPath;

    /**
     * Sets up the Java compiler ready to compile a submission.
     *
     * @param classPath class path to use when compiling submission
     * @throws IOException if loading the expected class files fails
     */
    public JavaCompilation(String classPath) {
        this.classPath = classPath;
    }

    /**
     * Attempts to compile the given submission.
     *
     * Outputs the compiled byte code to a "bin/" directory inside the
     * submission directory if successful, and creates a non-weighted test in
     * the results JSON containing the result of the compilation attempt.
     *
     * @param submission submission containing files to compile
     * @return submission, with compiled code in a "bin/" directory
     */
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
