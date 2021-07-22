package com.microsoft.censum.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 * Represents a GC log file, which may be a {@link SingleGCLogFile} or a {@link RotatingGCLogFile}.
 */
public abstract class GCLogFile extends FileDataSource<String> {

    /**
     * The value used for the implementation of {@link #endOfData()}.
     */
    public static final String END_OF_DATA_SENTINAL = "END_OF_DATA_SENTINAL";

    private final boolean unifiedFormat;

    /**
     * Subclass only.
     * @param path The path to the GCLogFile or, in the case of rotating log files, the parent directory.
     * @param unifiedFormat Whether the log file is in unified logging (JEP 158) format
     */
    protected GCLogFile(Path path, boolean unifiedFormat) {
        super(path);
        this.unifiedFormat = unifiedFormat;
    }

    /**
     * Returns {@code true} if this GCLogFile is written in unified logging (JEP 158) format.
     * @return {@code true} if the log file is in unified logging format.
     */
    public boolean isUnifiedFormat() { return unifiedFormat; }

    @Override
    public final String endOfData() {
        return END_OF_DATA_SENTINAL;
    }

    // match a line starting with a unified logging decorator,
    // e.g., [0.011s][info ][gc            ] Using G1
    // But have to watch out for things like [ParNew...
    private static final Pattern LINE_STARTS_WITH_DECORATOR = Pattern.compile("^\\[\\d.+?\\]");
    private static final int SHOULD_HAVE_SEEN_A_UNIFIED_DECORATOR_BY_THIS_LINE_IN_THE_LOG = 25;

    /**
     * This method is used to determine whether or not the log file uses the unified log format
     * by looking for lines starting with the unified logging decorator. This method is called from
     * the constructors of the subclasses.
     * @param stream A stream of lines from the file.
     * @return {@code true} if the file uses the unified log format.
     * @throws IOException Thrown from reading the stream.
     */
    /* package */ static boolean isUnifiedLogging(Stream<String> stream) throws IOException {
        return firstNLines(stream, SHOULD_HAVE_SEEN_A_UNIFIED_DECORATOR_BY_THIS_LINE_IN_THE_LOG)
                .map(LINE_STARTS_WITH_DECORATOR::matcher)
                .anyMatch(Matcher::find);
    }

    private static Stream<String> firstNLines(Stream<String> stream, int limit) {
        return stream.
                filter(Objects::nonNull).
                map(String::trim).
                filter(s -> s.length() > 0).
                limit(limit);
    }
}
