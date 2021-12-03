// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.stream.Collectors.toList;

/**
 * Meta-data about a {@link FileDataSource}.
 */
public class RotatingLogFileMetadata extends LogFileMetadata {

    private static final Logger LOG = Logger.getLogger(RotatingLogFileMetadata.class.getName());

    private List<LogFileSegment> segments;

    public RotatingLogFileMetadata(Path path) throws IOException {
        super(path);
    }

    public Stream<LogFileSegment> logFiles() {
        if ( segments == null) {
            if ( isPlainText() || isDirectory())
                findSegments();
            else if ( isZip())
                findZIPSegments();
            else {
                LOG.warning("unknown log file format");
                segments = new ArrayList<>();
            }
        }
        return segments.stream();
    }

    private void findZIPSegments() {
        try (var zipfile = new ZipFile(getPath().toFile())) {
            segments = zipfile.stream()
                    .filter(zipEntry -> !zipEntry.isDirectory())
                    .map(ZipEntry::getName)
                    .map(name -> new GCLogFileZipSegment(getPath(),name))
                    .collect(toList());
        } catch (IOException ioe) {
            LOG.warning(ioe.getMessage());
        }
    }

    /**
     * Return the number of files. Useful if the file is a compressed file which may
     * contain multiple entries.
     * @return The number of files in the file.
     */
    public int getNumberOfFiles() {
        if ( this.segments == null)
            if ( isZip())
                findZIPSegments();
            else
                findSegments();
            return this.segments.size();
    }

    private String getBasePattern() {

        String fileName = getPath().getFileName().toString();
        String[] bits = fileName.split("\\.");

        if ( "current".equals(bits[bits.length - 1]) || ( bits[bits.length - 1].matches("\\.\\d+$"))) {
            int index = 0;
            StringBuilder base = new StringBuilder(bits[index++]);
            while( ! bits[index].matches("\\.\\d+$")) {
                base.append(bits[index++]).append(".");
            }
            fileName = base.deleteCharAt(base.length() - 1).toString();
        }
        return fileName;
    }

    private void findSegments() {
        segments = new ArrayList<>();
        try {
            if (getPath().toFile().isDirectory()) {
                Files.list(getPath()).map(GCLogFileSegment::new).forEach(segments::add);
            }
            else {
                Files.list(getPath().getParent())
                        .filter(file -> file.getFileName().toString().startsWith(getBasePattern()))
                        .map(p -> new GCLogFileSegment(p)).forEach(segments::add);
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING,"Unable to find log segments.", ioe);
        }
        orderSegments();
    }

    private void orderSegments() {

        if (segments.size() < 2) {
            return;
        }

        LinkedList<LogFileSegment> orderedList = new LinkedList<>();
        List<LogFileSegment> workingList = new ArrayList<>();
        workingList.addAll(segments);

        // Find current
        String basePattern = getBasePattern();
        LogFileSegment current = workingList.stream()
                .filter( segment -> segment.getPath().toFile().getName().equals(basePattern))
                .findFirst().get();

        orderedList.addFirst(current);
        workingList = removeIneligibleSegments (workingList, current);
        while ( ! workingList.isEmpty()) {
            current = workingList.stream()
                    .max(Comparator.comparing(LogFileSegment::getEndTime))
                    .get();
            orderedList.addFirst(current);
            workingList = removeIneligibleSegments (workingList, current);
        }

        segments = orderedList;
    }

    private List<LogFileSegment> removeIneligibleSegments(final List<LogFileSegment> logFileSegments, final LogFileSegment current) {
        return logFileSegments.stream()
                .filter( segment -> segment.getEndTime() <= current.getStartTime())
                .collect(toList());
    }
}
