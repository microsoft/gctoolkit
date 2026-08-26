// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.util.Objects;

/**
 * Resource limits applied while reading garbage collection logs.
 *
 * <p>Limits are enforced for every top-level stream operation. Applications that need to process
 * larger trusted logs can supply higher finite values through the log-file constructors.
 */
public final class LogFileReadLimits {

    /** Default maximum number of bytes read from plaintext or emitted by decompression. */
    public static final long DEFAULT_MAX_EXPANDED_BYTES = 1024L * 1024L * 1024L;

    /** Default maximum number of bytes read from one compressed input. */
    public static final long DEFAULT_MAX_COMPRESSED_BYTES = 1024L * 1024L * 1024L;

    /** Default maximum number of decoded characters in one line. */
    public static final int DEFAULT_MAX_LINE_CHARACTERS = 1024 * 1024;

    /** Default maximum ratio of expanded bytes to compressed bytes. */
    public static final double DEFAULT_MAX_COMPRESSION_RATIO = 100.0d;

    /** Default expanded-byte threshold before compression-ratio enforcement begins. */
    public static final long DEFAULT_COMPRESSION_RATIO_GRACE_BYTES = 1024L * 1024L;

    /** Default maximum number of file entries in a rotating ZIP log. */
    public static final int DEFAULT_MAX_ARCHIVE_ENTRIES = 1024;

    /** Default maximum number of bytes in a ZIP central directory. */
    public static final long DEFAULT_MAX_ARCHIVE_METADATA_BYTES = 16L * 1024L * 1024L;

    /** Default maximum number of bytes in one GZIP member header. */
    public static final int DEFAULT_MAX_GZIP_HEADER_BYTES = 64 * 1024;

    private static final LogFileReadLimits DEFAULTS = new LogFileReadLimits(
            DEFAULT_MAX_COMPRESSED_BYTES,
            DEFAULT_MAX_EXPANDED_BYTES,
            DEFAULT_MAX_LINE_CHARACTERS,
            DEFAULT_MAX_COMPRESSION_RATIO,
            DEFAULT_COMPRESSION_RATIO_GRACE_BYTES,
            DEFAULT_MAX_ARCHIVE_ENTRIES,
            DEFAULT_MAX_ARCHIVE_METADATA_BYTES,
            DEFAULT_MAX_GZIP_HEADER_BYTES);

    private final long maxCompressedBytes;
    private final long maxExpandedBytes;
    private final int maxLineCharacters;
    private final double maxCompressionRatio;
    private final long compressionRatioGraceBytes;
    private final int maxArchiveEntries;
    private final long maxArchiveMetadataBytes;
    private final int maxGzipHeaderBytes;

    /**
     * Creates a set of finite log-file read limits.
     *
     * @param maxExpandedBytes maximum plaintext or expanded bytes per top-level stream operation
     * @param maxLineCharacters maximum decoded characters in one line
     * @param maxCompressionRatio maximum expanded-to-compressed byte ratio
     * @param compressionRatioGraceBytes expanded bytes allowed before ratio enforcement begins
     * @param maxArchiveEntries maximum file entries in a rotating ZIP log
     */
    public LogFileReadLimits(
            long maxExpandedBytes,
            int maxLineCharacters,
            double maxCompressionRatio,
            long compressionRatioGraceBytes,
            int maxArchiveEntries) {
        this(
                maxExpandedBytes,
                maxExpandedBytes,
                maxLineCharacters,
                maxCompressionRatio,
                compressionRatioGraceBytes,
                maxArchiveEntries,
                DEFAULT_MAX_ARCHIVE_METADATA_BYTES,
                DEFAULT_MAX_GZIP_HEADER_BYTES);
    }

    /**
     * Creates a set of finite log-file read limits, including compressed-input limits.
     *
     * @param maxCompressedBytes maximum bytes read from one compressed input
     * @param maxExpandedBytes maximum plaintext or expanded bytes per top-level stream operation
     * @param maxLineCharacters maximum decoded characters in one line
     * @param maxCompressionRatio maximum expanded-to-compressed byte ratio
     * @param compressionRatioGraceBytes expanded bytes allowed before ratio enforcement begins
     * @param maxArchiveEntries maximum ZIP entries, GZIP members, or rotating log segments
     * @param maxGzipHeaderBytes maximum bytes in one GZIP member header
     */
    public LogFileReadLimits(
            long maxCompressedBytes,
            long maxExpandedBytes,
            int maxLineCharacters,
            double maxCompressionRatio,
            long compressionRatioGraceBytes,
            int maxArchiveEntries,
            int maxGzipHeaderBytes) {
        this(
                maxCompressedBytes,
                maxExpandedBytes,
                maxLineCharacters,
                maxCompressionRatio,
                compressionRatioGraceBytes,
                maxArchiveEntries,
                DEFAULT_MAX_ARCHIVE_METADATA_BYTES,
                maxGzipHeaderBytes);
    }

    /**
     * Creates a set of finite log-file read limits, including archive metadata limits.
     *
     * @param maxCompressedBytes maximum bytes read from one compressed input
     * @param maxExpandedBytes maximum plaintext or expanded bytes per top-level stream operation
     * @param maxLineCharacters maximum decoded characters in one line
     * @param maxCompressionRatio maximum expanded-to-compressed byte ratio
     * @param compressionRatioGraceBytes expanded bytes allowed before ratio enforcement begins
     * @param maxArchiveEntries maximum ZIP entries, GZIP members, or rotating log segments
     * @param maxArchiveMetadataBytes maximum bytes in a ZIP central directory
     * @param maxGzipHeaderBytes maximum bytes in one GZIP member header
     */
    public LogFileReadLimits(
            long maxCompressedBytes,
            long maxExpandedBytes,
            int maxLineCharacters,
            double maxCompressionRatio,
            long compressionRatioGraceBytes,
            int maxArchiveEntries,
            long maxArchiveMetadataBytes,
            int maxGzipHeaderBytes) {
        if (maxCompressedBytes <= 0) {
            throw new IllegalArgumentException("maxCompressedBytes must be positive");
        }
        if (maxExpandedBytes <= 0) {
            throw new IllegalArgumentException("maxExpandedBytes must be positive");
        }
        if (maxLineCharacters <= 0) {
            throw new IllegalArgumentException("maxLineCharacters must be positive");
        }
        if (!Double.isFinite(maxCompressionRatio) || maxCompressionRatio <= 0.0d) {
            throw new IllegalArgumentException("maxCompressionRatio must be positive and finite");
        }
        if (compressionRatioGraceBytes <= 0) {
            throw new IllegalArgumentException("compressionRatioGraceBytes must be positive");
        }
        if (maxArchiveEntries <= 0) {
            throw new IllegalArgumentException("maxArchiveEntries must be positive");
        }
        if (maxArchiveMetadataBytes <= 0) {
            throw new IllegalArgumentException("maxArchiveMetadataBytes must be positive");
        }
        if (maxGzipHeaderBytes <= 0) {
            throw new IllegalArgumentException("maxGzipHeaderBytes must be positive");
        }
        this.maxCompressedBytes = maxCompressedBytes;
        this.maxExpandedBytes = maxExpandedBytes;
        this.maxLineCharacters = maxLineCharacters;
        this.maxCompressionRatio = maxCompressionRatio;
        this.compressionRatioGraceBytes = compressionRatioGraceBytes;
        this.maxArchiveEntries = maxArchiveEntries;
        this.maxArchiveMetadataBytes = maxArchiveMetadataBytes;
        this.maxGzipHeaderBytes = maxGzipHeaderBytes;
    }

    /**
     * Returns the secure default limits.
     *
     * @return immutable default limits
     */
    public static LogFileReadLimits defaults() {
        return DEFAULTS;
    }

    /**
     * Returns the maximum number of bytes read from one compressed input.
     *
     * @return maximum compressed bytes
     */
    public long getMaxCompressedBytes() {
        return maxCompressedBytes;
    }

    /**
     * Returns the maximum number of plaintext or expanded bytes per top-level stream operation.
     *
     * @return maximum expanded bytes
     */
    public long getMaxExpandedBytes() {
        return maxExpandedBytes;
    }

    /**
     * Returns the maximum number of decoded characters in one line.
     *
     * @return maximum line characters
     */
    public int getMaxLineCharacters() {
        return maxLineCharacters;
    }

    /**
     * Returns the maximum expanded-to-compressed byte ratio.
     *
     * @return maximum compression ratio
     */
    public double getMaxCompressionRatio() {
        return maxCompressionRatio;
    }

    /**
     * Returns the expanded-byte threshold before compression-ratio enforcement begins.
     *
     * @return compression-ratio grace bytes
     */
    public long getCompressionRatioGraceBytes() {
        return compressionRatioGraceBytes;
    }

    /**
     * Returns the maximum number of entries accepted in a rotating ZIP log.
     *
     * @return maximum archive entries
     */
    public int getMaxArchiveEntries() {
        return maxArchiveEntries;
    }

    /**
     * Returns the maximum number of bytes accepted in a ZIP central directory.
     *
     * @return maximum ZIP central-directory bytes
     */
    public long getMaxArchiveMetadataBytes() {
        return maxArchiveMetadataBytes;
    }

    /**
     * Returns the maximum number of bytes accepted in one GZIP member header.
     *
     * @return maximum GZIP header bytes
     */
    public int getMaxGzipHeaderBytes() {
        return maxGzipHeaderBytes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LogFileReadLimits)) {
            return false;
        }
        LogFileReadLimits that = (LogFileReadLimits) other;
        return maxCompressedBytes == that.maxCompressedBytes
                && maxExpandedBytes == that.maxExpandedBytes
                && maxLineCharacters == that.maxLineCharacters
                && Double.compare(maxCompressionRatio, that.maxCompressionRatio) == 0
                && compressionRatioGraceBytes == that.compressionRatioGraceBytes
                && maxArchiveEntries == that.maxArchiveEntries
                && maxArchiveMetadataBytes == that.maxArchiveMetadataBytes
                && maxGzipHeaderBytes == that.maxGzipHeaderBytes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                maxCompressedBytes,
                maxExpandedBytes,
                maxLineCharacters,
                maxCompressionRatio,
                compressionRatioGraceBytes,
                maxArchiveEntries,
                maxArchiveMetadataBytes,
                maxGzipHeaderBytes);
    }

    @Override
    public String toString() {
        return "LogFileReadLimits{"
                + "maxCompressedBytes=" + maxCompressedBytes
                + ", maxExpandedBytes=" + maxExpandedBytes
                + ", maxLineCharacters=" + maxLineCharacters
                + ", maxCompressionRatio=" + maxCompressionRatio
                + ", compressionRatioGraceBytes=" + compressionRatioGraceBytes
                + ", maxArchiveEntries=" + maxArchiveEntries
                + ", maxArchiveMetadataBytes=" + maxArchiveMetadataBytes
                + ", maxGzipHeaderBytes=" + maxGzipHeaderBytes
                + '}';
    }
}
