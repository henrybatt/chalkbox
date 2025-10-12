package chalkbox.source;

import java.util.List;

public class Solution extends Source {

    public Solution(String baseDirectory, List<String> classPath) {
        super("solution", baseDirectory, classPath);
    }

    public Solution(String name, String baseDirectory, List<String> classPath) {
        super(name, baseDirectory, classPath);
    }

//    public static Solution fromDiff(String name, String baseDirectory, String classPath, ... patch)
}
