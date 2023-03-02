// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine;
import com.microsoft.gctoolkit.jvm.PreUnifiedJavaVirtualMachine;
import com.microsoft.gctoolkit.jvm.UnifiedJavaVirtualMachine;
import com.microsoft.gctoolkit.parser.datatype.TripleState;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.ServiceLoader.Provider;
import static java.util.ServiceLoader.load;


/**
 * Represents a GC log file, which may be a {@link SingleGCLogFile} or a {@link RotatingGCLogFile}.
 */
public abstract class GCLogFile extends FileDataSource<String> {

    private static final Logger LOGGER = Logger.getLogger(FileDataSource.class.getName());

    /**
     * The value used for the implementation of {@link #endOfData()}.
     */
    public static final String END_OF_DATA_SENTINEL = "END_OF_DATA_SENTINEL";

    private Diary diary;
    private TripleState unifiedFormat = TripleState.UNKNOWN;
    private JavaVirtualMachine jvm = null;

    /**
     * Subclass only.
     * @param path The path to the GCLogFile or, in the case of rotating log files, the parent directory.
     */
    protected GCLogFile(Path path) {
        super(path);
    }

    /**
     * Return the relevant JavaVirtualMachine implementation
     */
    public JavaVirtualMachine getJavaVirtualMachine() {
        if ( jvm == null)
            jvm = (isUnified()) ? new UnifiedJavaVirtualMachine() : new PreUnifiedJavaVirtualMachine();
        jvm.accepts(this);
        return jvm;
    }

    /**
     * Returns {@code true} if this GCLogFile is written in unified logging (JEP 158) format.
     * @return {@code true} if the log file is in unified logging format.
     */
    public boolean isUnified() {
        if ( ! unifiedFormat.isKnown())
            unifiedFormat = discoverFormat();
        return unifiedFormat.isTrue();
    }

    private Diarizer diarizer() {
        ServiceLoader<Diarizer> serviceLoader = load(Diarizer.class);
        if (serviceLoader.findFirst().isPresent()) {
            return serviceLoader
                    .stream()
                    .map(Provider::get)
                    .filter(p -> p.isUnified() == isUnified())
                    .findFirst()
                    .orElseThrow(() -> new ServiceConfigurationError("Unable to find a suitable provider to create a diary"));
        } else {
            try {
                String clazzName = (isUnified()) ? "com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer" : "com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer";
                Class clazz = Class.forName(clazzName, true, Thread.currentThread().getContextClassLoader());
                return (Diarizer) clazz.getConstructors()[0].newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ServiceConfigurationError("Unable to find a suitable provider to create a diary");
            }
        }
    }

    /**
     *
     * @return the computed diary
     */
    public Diary diary() throws IOException {
        if ( diary == null) {
            Diarizer diarizer = diarizer();
            stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> s.length() > 0)
                    .map(diarizer::diarize)
                    .filter(completed -> completed)
                    .findFirst();
            this.diary = diarizer.getDiary();
        }
        return diary;
    }

    @Override
    public final String endOfData() {
        return END_OF_DATA_SENTINEL;
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
     * @return {@code true} if the file uses the unified log format.
     * @throws IOException Thrown from reading the stream.
     */
    private TripleState discoverFormat() {
        try {
            boolean isUnified = firstNLines(stream(), SHOULD_HAVE_SEEN_A_UNIFIED_DECORATOR_BY_THIS_LINE_IN_THE_LOG)
                    .map(LINE_STARTS_WITH_DECORATOR::matcher)
                    .anyMatch(Matcher::find);
            return TripleState.valueOf(isUnified);
        } catch(IOException ioe) {
            LOGGER.log(Level.SEVERE, "Unable to determine log file format", ioe);
        }
        return TripleState.UNKNOWN;
    }

    private Stream<String> firstNLines(Stream<String> stream, int limit) {
        return stream
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> s.length() > 0)
                .limit(limit);
    }
}
