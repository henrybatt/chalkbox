package chalkbox.java.conformance;

import java.util.*;

/**
 * A set of paths to fuzzy match.
 * Rules:
 *  - A string with a regular ending will match exactly that file
 *  - A string ending in / will match anything below that path
 *  - (TODO) * patterns
 * <p>
 * For example, the set of ignore paths:
 * {src/IgnoreMe.java, test/}
 * would ignore exactly src/IgnoreMe.java and nothing else,
 * and everything under test including test/IgnoreMe.java and test/anotherfile
 */
public class IgnorePaths {
    private Set<String> ignore = new HashSet<>();
    private IgnorePaths() {}

    /**
     * Construct a new ignore set from the given set of rules.
     *
     * @param ignore A set of string paths to ignore.
     * @return A matcher for the given paths.
     */
    public static IgnorePaths ofSet(Set<String> ignore) {
        IgnorePaths ignorePaths = new IgnorePaths();
        ignorePaths.ignore = ignore;
        return ignorePaths;
    }

    /**
     * Construct a new ignore set from the given set of rules.
     *
     * @param ignore A list of string paths to ignore.
     * @return A matcher for the given paths.
     */
    public static IgnorePaths ofStrings(String[] ignore) {
        return ofSet(new HashSet<>(List.of(ignore)));
    }

    /**
     * Whether the given path should be ignored according to this ignore set.
     *
     * @param path A potential path to check.
     * @return True iff the path should be ignored.
     */
    public boolean doIgnore(String path) {
        for (String ignored : ignore) {
            if (ignored.endsWith("/")) {
                if (path.startsWith(ignored)) {
                    return true;
                }
            } else {
                if (path.equals(ignored)) {
                    return true;
                }
            }
        }
        return false;
    }
}
