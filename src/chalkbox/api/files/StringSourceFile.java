package chalkbox.api.files;

import java.io.*;
import java.nio.file.Files;

/**
 * An implementation of a source file whose content comes from a String.
 */
public class StringSourceFile extends SourceFile {
    private String source;

    /**
     * Create a source file from a string.
     *
     * @param uri The unique resource identifier of the file (analogous to path)
     * @param source The string with which to base the source file
     */
    public StringSourceFile(String uri, String source) {
        super(uri);
        this.source = source;
    }

    public static StringSourceFile copyOf(SourceFile file) throws IOException {
        return new StringSourceFile(file.getURI(), file.getContent());
    }

    public StringSourceFile update(String contents) {
        return new StringSourceFile(this.getURI(), contents);
    }

    public void replaceAll(String regex, String replacement) {
        source = source.replaceAll(regex, replacement);
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new StringReader(source);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return source;
    }
}
