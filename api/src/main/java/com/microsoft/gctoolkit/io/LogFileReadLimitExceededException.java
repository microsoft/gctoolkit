// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.nio.file.Path;

/**
 * Indicates that a garbage collection log exceeded a configured resource limit while being read.
 *
 * <p>The exception is unchecked because stream I/O can fail lazily during a terminal stream
 * operation, after {@link DataSource#stream()} has returned.
 */
public final class LogFileReadLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The resource limit that was exceeded. */
    public enum LimitType {
        EXPANDED_BYTES,
        COMPRESSED_BYTES,
        LINE_CHARACTERS,
        COMPRESSION_RATIO,
        ARCHIVE_ENTRIES,
        ARCHIVE_METADATA_BYTES,
        COMPRESSED_MEMBERS,
        HEADER_BYTES
    }

    private final LimitType limitType;
    private final Path path;
    private final String archiveEntry;
    private final String configuredLimit;
    private final String observedValue;

    LogFileReadLimitExceededException(
            LimitType limitType,
            Path path,
            String archiveEntry,
            String configuredLimit,
            String observedValue) {
        super(message(limitType, path, archiveEntry, configuredLimit, observedValue));
        this.limitType = limitType;
        this.path = path;
        this.archiveEntry = archiveEntry;
        this.configuredLimit = configuredLimit;
        this.observedValue = observedValue;
    }

    private static String message(
            LimitType limitType,
            Path path,
            String archiveEntry,
            String configuredLimit,
            String observedValue) {
        String source = archiveEntry == null ? path.toString() : path + "!" + archiveEntry;
        return "Log file read limit exceeded for " + source + ": "
                + limitType + " limit " + configuredLimit + ", observed " + observedValue;
    }

    /**
     * Returns the type of resource limit that was exceeded.
     *
     * @return limit type
     */
    public LimitType getLimitType() {
        return limitType;
    }

    /**
     * Returns the path being read when the limit was exceeded.
     *
     * @return source path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the ZIP entry being read, if any.
     *
     * @return ZIP entry name, or {@code null} for a top-level source
     */
    public String getArchiveEntry() {
        return archiveEntry;
    }

    /**
     * Returns the configured limit rendered in the units identified by {@link #getLimitType()}.
     *
     * @return configured limit
     */
    public String getConfiguredLimit() {
        return configuredLimit;
    }

    /**
     * Returns the observed value rendered in the units identified by {@link #getLimitType()}.
     *
     * @return observed value
     */
    public String getObservedValue() {
        return observedValue;
    }
}
