package chalkbox.stages.compilation;

import chalkbox.api.common.java.Compiler;
import chalkbox.stages.Result;
import chalkbox.stages.StageException;
import chalkbox.stages.StageResult;
import chalkbox.source.Submission;
import com.google.common.flogger.FluentLogger;

import javax.tools.JavaFileObject;
import java.io.File;
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
public class Compilation {

    /** Message shown in output when submission compiles */
    private static final String FAILURE_MSG = "❌ Submission did not compile.";
    /** Message shown in output when submission does not compile */
    private static final String SUCCESS_MSG = "✅ Submission successfully compiled.";

    /** Class path to use to compile submissions */
    private String classPath;

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();


    /**
     * Sets up the Java compiler ready to compile a submission.
     *
     * @param classPath class path to use when compiling submission
     * @throws IOException if loading the expected class files fails
     */
    public Compilation(String classPath) {
        this.classPath = classPath;
    }

    /**
     * Attempts to compile the given submission.
     * <p>
     * Outputs the compiled byte code to a "bin/" directory inside the
     * submission directory if successful, and creates a non-weighted test in
     * the results JSON containing the result of the compilation attempt.
     * </p>
     * @param submission submission containing files to compile
     * @return submission, with compiled code in a "bin/" directory
     */
    public Result run(Submission submission) throws StageException, IOException {
        var result = new StageResult();

        var build = new File(submission.getSrcBuildPath());

        // Check if the folder exists
        if (!build.exists()) {
            if(build.mkdirs()) {
                logger.atInfo().log("build filepath created %s", build.getAbsolutePath());
            } else {
                logger.atSevere().log("unable to make build path %s", build.getAbsolutePath());
                throw new StageException("Unable to make build path");
            }
        }

        Iterable<? extends JavaFileObject> sourceFiles = submission.getSrcJavaFiles();
        if (sourceFiles == null) {
            throw new StageException("Couldn't load source files");
        }

        var output = new StringWriter();
        var classPath = submission.getSrcFolder() + File.pathSeparator + this.classPath;

        var success = Compiler.compile(sourceFiles, classPath, build.getAbsolutePath(), output);
        if (!success) {
            submission.setCanCompile(false);
            result.appendComment(FAILURE_MSG);
            result.appendComment(output.toString());
            return result;
        }

        submission.setCanCompile(true);
        result.appendComment(SUCCESS_MSG);
        return result;
    }
}
