package chalkbox.stages.functionality;

import chalkbox.source.Solution;
import chalkbox.source.Submission;
import chalkbox.stages.Result;
import chalkbox.stages.Stage;
import chalkbox.stages.StageException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Functionality implements Stage {

    @Override
    public Result run(Submission submission) throws StageException {
        // Not implemented
        return null;
    }

    @Override
    public Result run(Submission submission, List<Solution> solutions) throws StageException {
        // Not implemented
        return null;
    }

    /**
     * Run the tests on a submission.
     * <p>
     * If there were issues compiling the sample solution or the tests, or
     * the submission did not compile successfully, no action is taken.
     * <p>
     * Uses a JUnit listener to observe the passed/failed tests for each test
     * class. One Gradescope test is created for each JUnit test method, with
     * a mark of zero if the test failed, or a mark of
     * <code>stageWeighting / numTests</code> if the test passed, where
     * <code>stageWeighting</code> is the number of marks allocated to this
     * stage, and <code>numTests</code> is the total number of JUnit test
     * methods in all test classes.
     */
    @Override
    public Result run(Submission submission, Solution solution) throws StageException {
        // Compile the solution, tests and the submission
        try {
            var compilation = solution.compileSrc();
            if (!compilation.success()) {
                throw new StageException("Unable to compile solution: " + compilation.output());
            }
            compilation = solution.compileTest();
            if (!compilation.success()) {
                throw new StageException("Unable to compile tests: " + compilation.output());
            }
            compilation = submission.compileSrc();
            if (!compilation.success()) {
                throw new StageException("Unable to compile submission: " + compilation.output());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Path contains dependencies and the compile submission
        var classPath = solution.getClassPath() + File.pathSeparator + submission.getSrcBuildPath();

        return null;
    }
}

//        /* Summarise tests using the solution. */
//        for (String className : tests.getClasses("")) {
//            List<Data> results = JUnitRunner.runTests(className, classPath);
//            if (!results.isEmpty()) {
//                // Only summarise if there are tests in file, else skip.
//                testSummaries.put(className, new SolutionSummary(results.size(), (Double) results.getLast().get("classWeighting")));
//            }
//        }
//    }
//        Map<String, TestClassInfo> testInfo = new HashMap<>();
//        for (String className : tests.getClasses("")) {
//            if (!className.endsWith("Test")) {
//                continue;
//            }
//            List<Data> results = JUnitRunner.runTests(className, classPath);
//            // There are no tests in file - skip over it.
//            if (results.isEmpty()) {
//                continue;
//            }
//            /* Sort alphabetically by test class then test name */
//            results.sort(Comparator.comparing(o -> ((String) o.get("name"))));
//            int classPassing = 0;
//            /* Use test summaries to collect information even if test fails to compile. */
//            int classTests = testSummaries.get(className).totalTests;
//            double classWeighting = testSummaries.get(className).classWeight;
//            List<Data> testCases = new ArrayList<>();
//
//            for (Data result : results) {
//                boolean isPassing = (Integer) result.get("extra_data.passes") == 1;
//                // Get Test class JavaDoc
//                try {
//                    String testDescription = "";
//                    ClassJavadoc javaDoc = new SourceLoader(testCompiledOutputDirectory).getTestJavadoc(className);
//                    for (MethodJavadoc method : javaDoc.getMethods()) {
//                        if (method.getName().equals(result.get("name").toString().split("\\.")[1])) {
//                            testDescription += "" + method.getComment() + "\n";
//                        }
//                    }
//                    if (!isPassing && !testDescription.equals("")) {
//                        result.set("output", "❌ Test scenario fails\n### Scenario\n" + testDescription + "### Details\n" + result.get("output"));
//                    }
//                } catch (IOException ignored) {
//                    // Do Nothing
//                }
//
//                int testMultiplier = (Integer) result.get("weighting");
//                /* e.g. a test worth 5 "units" will increase the total number of tests by 5 */
//                totalNumTests += testMultiplier;
//                functionalityResults.add(result);
//                classPassing += (Integer) result.get("extra_data.passes") == 1 ? 1 : 0;
//                testCases.add(result);
//            }
//            testInfo.put(className, new TestClassInfo(className, classTests, classPassing, classWeighting, testCases));
//        }
//        if (totalNumTests == 0) {
//            return submission;
//        }
//
//        /* Mark awarded for passing a single test method (un-scaled by test multipliers) */
//        final double individualTestWeighting = 1d / totalNumTests * options.weighting;
//        int passingTests = 0;
//
//        for (Object o : functionalityResults) {
//            Data functionalityResult = (Data) o;
//            boolean didPass = (Integer) functionalityResult.get("extra_data.passes") == 1;
//            int testMultiplier = (Integer) functionalityResult.get("weighting");
//            functionalityResult.set("status", didPass ? "passed" : "failed");
//            passingTests += didPass ? 1 : 0;
////            functionalityResult.set("max_score", individualTestWeighting * testMultiplier);
//            testResults.add(functionalityResult);
//        }
//
//        String results = "| TestClass | Weighting | Passing Tests | Total |";
//        results += "\n| ----------- | ----------- | ----------- | ----------- |\n";
//        double total = 0;
//        double possible = 0;
//
//        for (TestClassInfo info : testInfo.values()) {
//            // Skip classes that have no tests.
//            if (info.totalTests <= 0) {
//                continue;
//            }
//            double score = (info.passingTests / (float) info.totalTests) * info.weight;
//            total += score;
//            possible += info.weight;
//            results += "| " + info.className + " | " + info.weight + " | " + info.passingTests + "/" + info.totalTests + " | " + score + "|\n";
//        }
//        double scaled = Math.ceil((total / possible) * options.weighting);
//
//        results += "\n$$\n\\dfrac{" + total + "}{" + possible + "} \\times " + options.weighting + " = " + scaled + "\n$$";
//
//        Data data = new Data();
//        data.set("name", "Functionality Tests");
//        data.set("score", scaled);
//        data.set("max_score", options.weighting);
//        data.set("output", results);
//        data.set("output_format", "md");
////        data.set("output", "You passed " + passingTests + " out of " + options.overrideTotalTests + " tests");
////        for (TestClassInfo info : testInfo.values()) {
////            data.set("output", data.get("output") + "\n" + info.toString());
////        }
//        data.set("visibility", "after_published");
//        testResults.add(0, data);
//
//        return submission;
//    }
//}
