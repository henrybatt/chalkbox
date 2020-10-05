package chalkbox.api.common.java;

import chalkbox.api.collections.Data;
import org.junit.runner.JUnitCore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * Utility class to execute a JUnit test.
 */
public class JUnitRunner {

    // Runs all tests in the given class and returns a single output object
    public static Data runTestsCombined(String className, String classPath) {
        return run(className, classPath).getResultsForClass();
    }

    // Runs all tests in the given class and returns an output object for each @Test
    public static List<Data> runTests(String className, String classPath) {
        return run(className, classPath).getIndividualResults();
    }

    private static JUnitListener run(String className, String classPath) {
        JUnitListener listener = new JUnitListener();
        JUnitCore runner = new JUnitCore();
        runner.addListener(listener);

        String[] classPathEntries = classPath.split(
                System.getProperty("path.separator"));
        URL[] classPathUrls = new URL[classPathEntries.length];
        for (int i = 0; i < classPathEntries.length; ++i) {
            try {
                classPathUrls[i] = new File(classPathEntries[i]).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        URLClassLoader classLoader = new URLClassLoader(classPathUrls);
        try {
            runner.run(classLoader.loadClass(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return listener;
    }

}
