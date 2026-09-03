// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import com.microsoft.gctoolkit.GCToolKit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.microsoft.gctoolkit.io.GCLogFile.END_OF_DATA_SENTINEL;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.ARCHIVE_ENTRIES;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.ARCHIVE_METADATA_BYTES;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.COMPRESSED_BYTES;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.COMPRESSED_MEMBERS;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.COMPRESSION_RATIO;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.EXPANDED_BYTES;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.HEADER_BYTES;
import static com.microsoft.gctoolkit.io.LogFileReadLimitExceededException.LimitType.LINE_CHARACTERS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleGCLogFileReadLimitsTest {

    private static final int DEFAULT_MAX_LINE_CHARACTERS =
            LogFileReadLimits.DEFAULT_MAX_LINE_CHARACTERS;
    private static final int HIGHLY_COMPRESSIBLE_LINE_COUNT = 2048;
    private static final String HIGHLY_COMPRESSIBLE_LINE = "a".repeat(1024);
    private static final LogFileReadLimits SMALL_LIMITS =
            new LogFileReadLimits(64 * 1024, 2048, 10_000.0d, 1024 * 1024, 16);

    @TempDir
    Path temporaryDirectory;

    @Test
    void rejectsLineLongerThanDefaultLimit() throws IOException {
        Path log = temporaryDirectory.resolve("long-line.log");
        Files.writeString(log, "a".repeat(DEFAULT_MAX_LINE_CHARACTERS + 1), StandardCharsets.UTF_8);

        LogFileReadLimitExceededException failure = assertThrows(LogFileReadLimitExceededException.class, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log).stream()) {
                stream.findFirst();
            }
        });
        assertEquals(LINE_CHARACTERS, failure.getLimitType());
        assertEquals(log, failure.getPath());
    }

    @Test
    void rejectsExcessiveGzipCompressionRatio() throws IOException {
        Path log = temporaryDirectory.resolve("high-ratio.log.gz");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new GZIPOutputStream(Files.newOutputStream(log)), StandardCharsets.UTF_8))) {
            for (int index = 0; index < HIGHLY_COMPRESSIBLE_LINE_COUNT; index++) {
                writer.write(HIGHLY_COMPRESSIBLE_LINE);
                writer.newLine();
            }
        }

        LogFileReadLimitExceededException failure = assertThrows(LogFileReadLimitExceededException.class, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log).stream()) {
                stream.count();
            }
        });
        assertEquals(COMPRESSION_RATIO, failure.getLimitType());
    }

    @Test
    void acceptsLineAtConfiguredLimitAndCommonLineEndings() throws IOException {
        Path log = temporaryDirectory.resolve("line-endings.log");
        Files.writeString(log, "ab\r\ncd\ref\néé", StandardCharsets.UTF_8);
        LogFileReadLimits limits = new LogFileReadLimits(128, 2, 100.0d, 64, 16);

        try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
            assertEquals(
                    List.of("ab", "cd", "ef", "éé", END_OF_DATA_SENTINEL),
                    stream.collect(Collectors.toList()));
        }
    }

    @Test
    void rejectsPlaintextExpandedBytesOverConfiguredLimit() throws IOException {
        Path log = temporaryDirectory.resolve("expanded.log");
        Files.writeString(log, repeatedLines(80, 1023), StandardCharsets.UTF_8);

        assertLimit(EXPANDED_BYTES, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsGzipExpandedBytesOverConfiguredLimitAcrossMembers() throws IOException {
        Path log = temporaryDirectory.resolve("concatenated.log.gz");
        writeGzipMember(log, repeatedLines(40, 1023), false);
        writeGzipMember(log, repeatedLines(40, 1023), true);

        assertLimit(EXPANDED_BYTES, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void acceptsExpandedBytesAtConfiguredLimit() throws IOException {
        Path log = temporaryDirectory.resolve("expanded-boundary.log");
        Files.writeString(log, repeatedLines(64, 1023), StandardCharsets.UTF_8);

        try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
            assertEquals(65L, stream.count());
        }
    }

    @Test
    void rejectsZipExpandedBytesOverConfiguredLimit() throws IOException {
        Path log = temporaryDirectory.resolve("expanded.log.zip");
        writeZip(log, Map.of("gc.log", repeatedLines(80, 1023)));

        assertLimit(EXPANDED_BYTES, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsLongLineInGzip() throws IOException {
        Path log = temporaryDirectory.resolve("long-line.log.gz");
        writeGzipMember(log, "a".repeat(2049), false);

        assertLimit(LINE_CHARACTERS, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.findFirst();
            }
        });
    }

    @Test
    void rejectsLongLineInZip() throws IOException {
        Path log = temporaryDirectory.resolve("long-line.log.zip");
        writeZip(log, Map.of("gc.log", "a".repeat(2049)));

        assertLimit(LINE_CHARACTERS, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.findFirst();
            }
        });
    }

    @Test
    void rejectsExcessiveZipCompressionRatio() throws IOException {
        Path log = temporaryDirectory.resolve("high-ratio.log.zip");
        writeZip(log, Map.of("gc.log", repeatedLines(2048, 1023)));

        assertLimit(COMPRESSION_RATIO, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void acceptsCompressionRatioWithinGraceThreshold() throws IOException {
        Path log = temporaryDirectory.resolve("ratio-grace.log.gz");
        writeGzipMember(log, "line\n", false);
        LogFileReadLimits limits = new LogFileReadLimits(1024, 128, 0.1d, 128, 16);

        try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
            assertEquals(2L, stream.count());
        }
    }

    @Test
    void rejectsHighRatioConcatenatedGzipMemberAfterLowRatioMember() throws IOException {
        Path log = temporaryDirectory.resolve("mixed-ratio-members.log.gz");
        writeGzipMember(log, randomAsciiLine(50_000), false);
        writeGzipMember(log, "a".repeat(100_000) + "\n", true);
        LogFileReadLimits limits = new LogFileReadLimits(256 * 1024, 200_000, 10.0d, 1024, 16);

        assertLimit(COMPRESSION_RATIO, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void appliesCompressionRatioGraceOnceAcrossGzipMembers() throws IOException {
        Path log = temporaryDirectory.resolve("split-grace-members.log.gz");
        for (int index = 0; index < 10; index++) {
            writeGzipMember(log, "a".repeat(1024), index != 0);
        }
        LogFileReadLimits limits = new LogFileReadLimits(32 * 1024, 16 * 1024, 2.0d, 1024, 16);

        assertLimit(COMPRESSION_RATIO, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void appliesCompressionRatioGraceOnceAcrossZipEntries() throws IOException {
        Path log = temporaryDirectory.resolve("split-grace-entries.zip");
        writeZip(log, Map.of(
                "gc.log.0", "[1.0s][info][gc] " + "a".repeat(800) + "\n",
                "gc.log.1", "[2.0s][info][gc] " + "a".repeat(800) + "\n",
                "gc.log", "[3.0s][info][gc] " + "a".repeat(800) + "\n"));
        LogFileReadLimits limits = new LogFileReadLimits(32 * 1024, 2048, 2.0d, 1024, 16);

        assertLimit(
                COMPRESSION_RATIO,
                () -> new RotatingGCLogFile(log, limits).getMetaData().getNumberOfFiles());
    }

    @Test
    void validatesGzipHeaderCrc() throws IOException {
        Path valid = temporaryDirectory.resolve("valid-header-crc.log.gz");
        writeGzipWithHeaderCrc(valid, "line\n", false);
        try (Stream<String> stream = new SingleGCLogFile(valid, SMALL_LIMITS).stream()) {
            assertEquals(2L, stream.count());
        }

        Path invalid = temporaryDirectory.resolve("invalid-header-crc.log.gz");
        writeGzipWithHeaderCrc(invalid, "line\n", true);
        assertThrows(UncheckedIOException.class, () -> {
            try (Stream<String> stream = new SingleGCLogFile(invalid, SMALL_LIMITS).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsExcessiveGzipMembersEvenWhenEmpty() throws IOException {
        Path log = temporaryDirectory.resolve("empty-members.log.gz");
        for (int index = 0; index < 5; index++) {
            writeGzipMember(log, "", index != 0);
        }
        LogFileReadLimits limits =
                new LogFileReadLimits(64 * 1024, 64 * 1024, 1024, 100.0d, 1024, 4, 1024);

        assertLimit(COMPRESSED_MEMBERS, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsOversizedGzipHeaderField() throws IOException {
        Path log = temporaryDirectory.resolve("long-header.log.gz");
        writeGzipWithFileName(log, "a".repeat(64), "line\n");
        LogFileReadLimits limits =
                new LogFileReadLimits(64 * 1024, 64 * 1024, 1024, 100.0d, 1024, 16, 32);

        assertLimit(HEADER_BYTES, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsOversizedCompressedGzipInput() throws IOException {
        Path log = temporaryDirectory.resolve("compressed-input.log.gz");
        writeGzipMember(log, randomAsciiLine(4096), false);
        LogFileReadLimits limits =
                new LogFileReadLimits(512, 16 * 1024, 16 * 1024, 100.0d, 1024, 16, 1024);

        assertLimit(COMPRESSED_BYTES, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void ignoresPayloadOfZipDirectoryEntry() throws IOException {
        Path log = temporaryDirectory.resolve("directory-payload.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(log))) {
            zip.putNextEntry(new ZipEntry("ignored/"));
            zip.write("a".repeat(128 * 1024).getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("gc.log"));
            zip.write("line\n".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        LogFileReadLimits limits = new LogFileReadLimits(1024, 128, 2.0d, 128, 16);

        try (Stream<String> stream = new SingleGCLogFile(log, limits).stream()) {
            assertEquals(2L, stream.count());
        }
    }

    @Test
    void readsFirstFileAfterZipDirectory() throws IOException {
        Path log = temporaryDirectory.resolve("directory-first.zip");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(log))) {
            zip.putNextEntry(new ZipEntry("logs/"));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("logs/gc.log"));
            zip.write("line\n".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
            assertEquals(List.of("line", END_OF_DATA_SENTINEL), stream.collect(Collectors.toList()));
        }
    }

    @Test
    void readsAndValidatesStoredZipEntry() throws IOException {
        Path log = temporaryDirectory.resolve("stored.zip");
        byte[] content = "line\n".getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(content);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(log))) {
            ZipEntry entry = new ZipEntry("gc.log");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
            zip.putNextEntry(entry);
            zip.write(content);
            zip.closeEntry();
        }

        try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
            assertEquals(2L, stream.count());
        }
    }

    @Test
    void readsStoredZipEntryWithDataDescriptor() throws IOException {
        Path log = temporaryDirectory.resolve("stored-descriptor.zip");
        byte[] content = "line\n".getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(content);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(log))) {
            ZipEntry entry = new ZipEntry("gc.log");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(content.length);
            entry.setCompressedSize(content.length);
            entry.setCrc(crc.getValue());
            zip.putNextEntry(entry);
            zip.write(content);
            zip.closeEntry();
        }
        Files.write(log, withStoredDataDescriptor(Files.readAllBytes(log)));

        try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
            assertEquals(2L, stream.count());
        }
    }

    @Test
    void rejectsEmptyZip() throws IOException {
        Path log = temporaryDirectory.resolve("empty.zip");
        try (ZipOutputStream ignored = new ZipOutputStream(Files.newOutputStream(log))) {
            // Empty archive.
        }

        assertThrows(IOException.class, () -> new SingleGCLogFile(log, SMALL_LIMITS).stream());
    }

    @Test
    void rejectsZipWithStaleCentralDirectoryCrc() throws IOException {
        Path log = temporaryDirectory.resolve("stale-central-crc.zip");
        writeZip(log, Map.of("gc.log", "line\n"));
        byte[] archive = Files.readAllBytes(log);
        int centralDirectory = findSignature(archive, 0x02014b50);
        archive[centralDirectory + 16] ^= 1;
        Files.write(log, archive);

        assertThrows(UncheckedIOException.class, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsZipWithForgedCentralDirectoryCompressedSize() throws IOException {
        Path log = temporaryDirectory.resolve("forged-compressed-size.zip");
        writeZip(log, Map.of("gc.log", "a".repeat(8192) + "\n"));
        byte[] archive = Files.readAllBytes(log);
        int centralDirectory = findSignature(archive, 0x02014b50);
        putUnsignedInt(archive, centralDirectory + 20, 1024 * 1024);
        Files.write(log, archive);
        LogFileReadLimits limits = new LogFileReadLimits(16 * 1024, 16 * 1024, 10_000.0d, 1024, 16);

        assertThrows(IOException.class, () -> new SingleGCLogFile(log, limits).stream());
    }

    @Test
    void rejectsOversizedZip64DeclaredEntryCount() throws IOException {
        Path log = temporaryDirectory.resolve("forged-zip64-count.zip");
        writeZip(log, Map.of("gc.log", "line\n"));
        Files.write(log, withZip64EntryCount(Files.readAllBytes(log), 500_000_000L));

        LogFileReadLimitExceededException failure = assertThrows(
                LogFileReadLimitExceededException.class,
                () -> new SingleGCLogFile(log, SMALL_LIMITS).stream());
        assertEquals(ARCHIVE_ENTRIES, failure.getLimitType());
    }

    @Test
    void rejectsOversizedCompressedZipInput() throws IOException {
        Path log = temporaryDirectory.resolve("compressed-input.zip");
        writeZip(log, Map.of("gc.log", randomAsciiLine(4096)));
        LogFileReadLimits limits =
                new LogFileReadLimits(512, 16 * 1024, 16 * 1024, 100.0d, 1024, 16, 1024);

        assertLimit(COMPRESSED_BYTES, () -> new SingleGCLogFile(log, limits).stream());
    }

    @Test
    void rejectsExcessiveZipControlRecords() throws IOException {
        Path log = temporaryDirectory.resolve("control-records.zip");
        Files.write(log, zipWithArchiveExtraRecords(5));
        LogFileReadLimits limits =
                new LogFileReadLimits(64 * 1024, 64 * 1024, 1024, 100.0d, 1024, 2, 1024);

        assertLimit(ARCHIVE_ENTRIES, () -> new SingleGCLogFile(log, limits).stream());
    }

    @Test
    void rejectsOversizedZipCentralDirectoryBeforeIndexing() throws IOException {
        Path log = temporaryDirectory.resolve("central-directory.zip");
        writeZip(log, Map.of("gc.log", "line\n"));
        LogFileReadLimits limits = new LogFileReadLimits(
                64 * 1024,
                64 * 1024,
                1024,
                100.0d,
                1024,
                16,
                32,
                1024);

        assertLimit(ARCHIVE_METADATA_BYTES, () -> new SingleGCLogFile(log, limits).stream());
    }

    @Test
    void rejectsExcessiveDirectorySegments() throws IOException {
        Path directory = temporaryDirectory.resolve("rotating");
        Files.createDirectory(directory);
        Files.writeString(directory.resolve("gc.log"), "[3.0s][info][gc] current\n");
        Files.writeString(directory.resolve("gc.log.0"), "[1.0s][info][gc] first\n");
        Files.writeString(directory.resolve("gc.log.1"), "[2.0s][info][gc] second\n");
        LogFileReadLimits limits =
                new LogFileReadLimits(64 * 1024, 64 * 1024, 1024, 100.0d, 1024, 2, 1024);

        assertLimit(
                ARCHIVE_ENTRIES,
                () -> new RotatingGCLogFile(directory, limits).getMetaData().getNumberOfFiles());
    }

    @Test
    void doesNotEmitSentinelAfterLimitFailure() throws IOException {
        Path log = temporaryDirectory.resolve("sentinel.log");
        Files.writeString(log, "ok\n" + "a".repeat(2049), StandardCharsets.UTF_8);
        List<String> consumed = new ArrayList<>();

        assertLimit(LINE_CHARACTERS, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.forEach(consumed::add);
            }
        });
        assertEquals(List.of("ok"), consumed);
        assertFalse(consumed.contains(END_OF_DATA_SENTINEL));
    }

    @Test
    void closesFileAfterLimitFailure() throws IOException {
        Path log = temporaryDirectory.resolve("closed-after-failure.log");
        Files.writeString(log, "a".repeat(2049), StandardCharsets.UTF_8);

        assertLimit(LINE_CHARACTERS, () -> {
            try (Stream<String> stream = new SingleGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.findFirst();
            }
        });
        assertDoesNotThrow(() -> Files.delete(log));
        assertFalse(Files.exists(log));
    }

    @Test
    void analysisPreservesLimitFailure() throws IOException {
        Path log = temporaryDirectory.resolve("analysis-limit.log");
        Files.writeString(log, "a".repeat(2049), StandardCharsets.UTF_8);

        assertLimit(
                LINE_CHARACTERS,
                () -> new GCToolKit().analyze(new SingleGCLogFile(log, SMALL_LIMITS)));
    }

    @Test
    void rejectsAggregateExpandedBytesAcrossRotatingZipEntries() throws IOException {
        Path log = temporaryDirectory.resolve("gc.log.zip");
        writeZip(log, Map.of(
                "gc.log.0", timestampedLines(1.0d, 40, 1000),
                "gc.log", timestampedLines(2.0d, 40, 1000)));

        assertLimit(EXPANDED_BYTES, () -> {
            try (Stream<String> stream = new RotatingGCLogFile(log, SMALL_LIMITS).stream()) {
                stream.count();
            }
        });
    }

    @Test
    void rejectsExcessiveRotatingZipEntries() throws IOException {
        Path log = temporaryDirectory.resolve("many-entries.zip");
        writeZip(log, Map.of(
                "gc.log.0", "[1.0s][info][gc] first\n",
                "gc.log.1", "[2.0s][info][gc] second\n",
                "gc.log", "[3.0s][info][gc] current\n"));
        LogFileReadLimits limits = new LogFileReadLimits(64 * 1024, 2048, 100.0d, 1024, 2);

        LogFileReadLimitExceededException failure = assertThrows(
                LogFileReadLimitExceededException.class,
                () -> new RotatingGCLogFile(log, limits).getMetaData().getNumberOfFiles());
        assertEquals(ARCHIVE_ENTRIES, failure.getLimitType());
    }

    @Test
    void validatesLimitConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(0, 1, 1.0d, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(1, 0, 1.0d, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(1, 1, Double.POSITIVE_INFINITY, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(1, 1, 1.0d, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(1, 1, 1.0d, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(0, 1, 1, 1.0d, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(1, 1, 1, 1.0d, 1, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new LogFileReadLimits(1, 1, 1, 1.0d, 1, 1, 0, 1));
    }

    @Test
    void exposesFiniteSecureDefaults() {
        LogFileReadLimits defaults = LogFileReadLimits.defaults();

        assertTrue(defaults.getMaxCompressedBytes() > 0);
        assertTrue(defaults.getMaxExpandedBytes() > 0);
        assertTrue(defaults.getMaxLineCharacters() > 0);
        assertTrue(Double.isFinite(defaults.getMaxCompressionRatio()));
        assertTrue(defaults.getCompressionRatioGraceBytes() > 0);
        assertTrue(defaults.getMaxArchiveEntries() > 0);
        assertTrue(defaults.getMaxArchiveMetadataBytes() > 0);
        assertTrue(defaults.getMaxGzipHeaderBytes() > 0);
    }

    @Test
    void enforcesExpandedBudgetAtomically() throws Exception {
        LogFileReadBudget budget = new LogFileReadBudget(1000);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int index = 0; index < 4; index++) {
                results.add(executor.submit(() -> {
                    start.await();
                    try {
                        budget.record(1000, temporaryDirectory.resolve("parallel.log"), null);
                        return true;
                    } catch (LogFileReadLimitExceededException expected) {
                        return false;
                    }
                }));
            }
            start.countDown();
            long successes = 0L;
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
            assertEquals(1L, successes);
        } finally {
            executor.shutdownNow();
        }
    }

    private static void assertLimit(
            LogFileReadLimitExceededException.LimitType expected,
            ThrowingOperation operation) {
        LogFileReadLimitExceededException failure =
                assertThrows(LogFileReadLimitExceededException.class, operation::run);
        assertEquals(expected, failure.getLimitType());
    }

    private static String repeatedLines(int lineCount, int lineLength) {
        return ("a".repeat(lineLength) + "\n").repeat(lineCount);
    }

    private static String timestampedLines(double timestamp, int lineCount, int lineLength) {
        StringBuilder content = new StringBuilder(lineCount * (lineLength + 32));
        for (int index = 0; index < lineCount; index++) {
            content.append('[')
                    .append(timestamp + (index / 1000.0d))
                    .append("s][info][gc] ")
                    .append("a".repeat(lineLength))
                    .append('\n');
        }
        return content.toString();
    }

    private static String randomAsciiLine(int length) {
        Random random = new Random(123456789L);
        StringBuilder line = new StringBuilder(length + 1);
        for (int index = 0; index < length; index++) {
            line.append((char) ('!' + random.nextInt('~' - '!' + 1)));
        }
        return line.append('\n').toString();
    }

    private static void writeGzipMember(Path path, String content, boolean append) throws IOException {
        StandardOpenOption[] options = append
                ? new StandardOpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                : new StandardOpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
        try (GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(path, options))) {
            gzip.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void writeGzipWithHeaderCrc(
            Path path,
            String content,
            boolean corruptChecksum) throws IOException {
        Path ordinaryGzip = path.resolveSibling(path.getFileName() + ".ordinary");
        writeGzipMember(ordinaryGzip, content, false);
        byte[] ordinary = Files.readAllBytes(ordinaryGzip);
        ordinary[3] |= 2;
        CRC32 headerCrc = new CRC32();
        headerCrc.update(ordinary, 0, 10);
        int checksum = (int) headerCrc.getValue() & 0xffff;
        if (corruptChecksum) {
            checksum ^= 1;
        }
        byte[] withHeaderCrc = new byte[ordinary.length + 2];
        System.arraycopy(ordinary, 0, withHeaderCrc, 0, 10);
        withHeaderCrc[10] = (byte) checksum;
        withHeaderCrc[11] = (byte) (checksum >>> 8);
        System.arraycopy(ordinary, 10, withHeaderCrc, 12, ordinary.length - 10);
        Files.write(path, withHeaderCrc);
        Files.delete(ordinaryGzip);
    }

    private static void writeGzipWithFileName(
            Path path,
            String fileName,
            String content) throws IOException {
        Path ordinaryGzip = path.resolveSibling(path.getFileName() + ".ordinary");
        writeGzipMember(ordinaryGzip, content, false);
        byte[] ordinary = Files.readAllBytes(ordinaryGzip);
        ordinary[3] |= 8;
        byte[] name = fileName.getBytes(StandardCharsets.ISO_8859_1);
        byte[] withName = new byte[ordinary.length + name.length + 1];
        System.arraycopy(ordinary, 0, withName, 0, 10);
        System.arraycopy(name, 0, withName, 10, name.length);
        System.arraycopy(ordinary, 10, withName, 11 + name.length, ordinary.length - 10);
        Files.write(path, withName);
        Files.delete(ordinaryGzip);
    }

    private static void writeZip(Path path, Map<String, String> entries) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
    }

    private static byte[] withZip64EntryCount(byte[] ordinaryZip, long entryCount) {
        int end = findSignature(ordinaryZip, 0x06054b50);
        long directorySize = unsignedInt(ordinaryZip, end + 12);
        long directoryOffset = unsignedInt(ordinaryZip, end + 16);
        byte[] result = new byte[ordinaryZip.length + 76];
        System.arraycopy(ordinaryZip, 0, result, 0, end);

        int zip64End = end;
        putUnsignedInt(result, zip64End, 0x06064b50L);
        putLong(result, zip64End + 4, 44L);
        putUnsignedShort(result, zip64End + 12, 45);
        putUnsignedShort(result, zip64End + 14, 45);
        putUnsignedInt(result, zip64End + 16, 0L);
        putUnsignedInt(result, zip64End + 20, 0L);
        putLong(result, zip64End + 24, entryCount);
        putLong(result, zip64End + 32, entryCount);
        putLong(result, zip64End + 40, directorySize);
        putLong(result, zip64End + 48, directoryOffset);

        int locator = zip64End + 56;
        putUnsignedInt(result, locator, 0x07064b50L);
        putUnsignedInt(result, locator + 4, 0L);
        putLong(result, locator + 8, zip64End);
        putUnsignedInt(result, locator + 16, 1L);

        int newEnd = locator + 20;
        System.arraycopy(ordinaryZip, end, result, newEnd, ordinaryZip.length - end);
        putUnsignedShort(result, newEnd + 8, 0xffff);
        putUnsignedShort(result, newEnd + 10, 0xffff);
        return result;
    }

    private static byte[] withStoredDataDescriptor(byte[] ordinaryZip) {
        int centralDirectory = findSignature(ordinaryZip, 0x02014b50);
        int end = findSignature(ordinaryZip, 0x06054b50);
        int localHeader = (int) unsignedInt(ordinaryZip, centralDirectory + 42);
        int nameLength = Byte.toUnsignedInt(ordinaryZip[localHeader + 26])
                | (Byte.toUnsignedInt(ordinaryZip[localHeader + 27]) << 8);
        int extraLength = Byte.toUnsignedInt(ordinaryZip[localHeader + 28])
                | (Byte.toUnsignedInt(ordinaryZip[localHeader + 29]) << 8);
        int dataStart = localHeader + 30 + nameLength + extraLength;
        int compressedSize = (int) unsignedInt(ordinaryZip, centralDirectory + 20);
        int dataEnd = dataStart + compressedSize;
        long crc = unsignedInt(ordinaryZip, centralDirectory + 16);
        long expandedSize = unsignedInt(ordinaryZip, centralDirectory + 24);

        byte[] result = new byte[ordinaryZip.length + 16];
        System.arraycopy(ordinaryZip, 0, result, 0, dataEnd);
        putUnsignedInt(result, dataEnd, 0x08074b50L);
        putUnsignedInt(result, dataEnd + 4, crc);
        putUnsignedInt(result, dataEnd + 8, compressedSize);
        putUnsignedInt(result, dataEnd + 12, expandedSize);
        System.arraycopy(
                ordinaryZip,
                dataEnd,
                result,
                dataEnd + 16,
                ordinaryZip.length - dataEnd);

        result[localHeader + 6] |= 8;
        for (int offset = 14; offset < 26; offset++) {
            result[localHeader + offset] = 0;
        }
        int shiftedCentralDirectory = centralDirectory + 16;
        result[shiftedCentralDirectory + 8] |= 8;
        int shiftedEnd = end + 16;
        putUnsignedInt(result, shiftedEnd + 16, shiftedCentralDirectory);
        return result;
    }

    private static byte[] zipWithArchiveExtraRecords(int recordCount) {
        int directorySize = recordCount * 8;
        byte[] archive = new byte[directorySize + 22];
        for (int index = 0; index < recordCount; index++) {
            putUnsignedInt(archive, index * 8, 0x08064b50L);
            putUnsignedInt(archive, index * 8 + 4, 0L);
        }
        putUnsignedInt(archive, directorySize, 0x06054b50L);
        putUnsignedInt(archive, directorySize + 12, directorySize);
        putUnsignedInt(archive, directorySize + 16, 0L);
        return archive;
    }

    private static int findSignature(byte[] bytes, int signature) {
        for (int index = 0; index <= bytes.length - 4; index++) {
            if ((int) unsignedInt(bytes, index) == signature) {
                return index;
            }
        }
        throw new IllegalArgumentException("ZIP signature not found");
    }

    private static long unsignedInt(byte[] bytes, int offset) {
        return (long) Byte.toUnsignedInt(bytes[offset])
                | ((long) Byte.toUnsignedInt(bytes[offset + 1]) << 8)
                | ((long) Byte.toUnsignedInt(bytes[offset + 2]) << 16)
                | ((long) Byte.toUnsignedInt(bytes[offset + 3]) << 24);
    }

    private static void putUnsignedShort(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >>> 8);
    }

    private static void putUnsignedInt(byte[] bytes, int offset, long value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >>> 8);
        bytes[offset + 2] = (byte) (value >>> 16);
        bytes[offset + 3] = (byte) (value >>> 24);
    }

    private static void putLong(byte[] bytes, int offset, long value) {
        putUnsignedInt(bytes, offset, value);
        putUnsignedInt(bytes, offset + 4, value >>> 32);
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }
}
