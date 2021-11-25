// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
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

    private final List<GCLogFileSegment> segments = new ArrayList<>();

    public RotatingLogFileMetadata(Path path) throws IOException {
        super(path);
    }

    public Stream<Path> logFiles() throws IOException {
        Function<GCLogFileSegment, Path> function = GCLogFileSegment -> GCLogFileSegment.getPath();
        return segments.stream().map(function);
    }

    void magic(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            findSegments(path);
        } else if (Files.isRegularFile(path)) {
            discoverFormat();
            if (isZip() || isGZip()) {
                segments.add(new GCLogFileSegment(path));
            } else
                findSegments(path);
        } else
            throw new IOException(path + " is neither a directory or a regular file.");
    }


    private void findZIPSegments() {
        try (var zipfile = new ZipFile(getPath().toFile())) {
            Function<ZipEntry,String> mapToName = zipEntry -> zipEntry.getName();
            List<String> entries = zipfile.stream().filter(zipEntry -> !zipEntry.isDirectory()).map(mapToName).collect(toList());
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
            return this.segments.size();
    }

    public void findSegments(Path path) throws IOException {
        if ( path.toFile().isDirectory()) {
            Files.list(path).map(entry -> new GCLogFileSegment(entry)).forEach(segments::add);
        } else {
            String[] bits = path.getFileName().toString().split("\\.");
            StringBuilder pattern = new StringBuilder(bits[0]).append(".");
            if (bits[bits.length - 1].matches("\\d+")) {
                for (int i = 1; i < bits.length - 1; i++)
                    pattern.append(bits[i]).append(".");
                pattern.deleteCharAt(pattern.length() - 1);
            }
            String base = pattern.toString();
            Files.list(path.getParent()).filter(file -> file.getFileName().toString().startsWith(base)).map(p -> new GCLogFileSegment(p)).forEach(segments::add);
        }
        orderSegments();
    }

    private static void findZIPSegments(Path path) {
        try (var zipfile = new ZipFile(path.toFile())) {
            Function<ZipEntry,String> mapToName = zipEntry -> zipEntry.getName();
            zipfile.stream().filter(zipEntry -> !zipEntry.isDirectory()).map(mapToName).forEach(System.out::println);
            ZipEntry entry = zipfile.getEntry("rollover.log.1");
            //new File(entry.getName()).toPath();
            //List<String> entries = zipfile.stream().filter(zipEntry -> !zipEntry.isDirectory()).map(mapToName).collect(toList());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());;
            ioe.printStackTrace();
        }
    }

    private List<GCLogFileSegment> orderSegments() {
        // Unified rotation: jdk11/src/hotspot/share/logging/logFileOutput.cpp
        //     Output is always to named file, e.g. 'gc.log' if given -Xlog:gc*:file=gc.log::filecount=5
        //     When gc.log is full, archive as gc.log.<_current_file>. Before the of gc.log to gc.log.<_current_file>,
        //     an attempt is made to delete an existing gc.log.<_current_file>
        //     Increment _current_file. if _current_file == filecount, set _current_file to zero
        //         - JVM always begins archiving at gc.log.0
        //         - gc.log is always the current log
        //         - Once filecount files have been archived, archive begins at zero again
        //
        // Pre-unified rotation: jdk8/hotspot/src/share/vm/utilities/ostream.cpp
        //     rotate file in names extended_filename.0, extended_filename.1, ...,
        //     extended_filename.<NumberOfGCLogFiles - 1>. Current rotation file name will
        //     have a form of extended_filename.<i>.current where i is the current rotation
        //     file number. After it reaches max file size, the file will be saved and renamed
        //     with .current removed from its tail.

        if (segments.size() < 2) {
            return segments;
        }

        //LinkedList<GarbageCollectionLogFileSegment> orderedSeqments = new LinkedList<>();
        GCLogFileSegment[] workingList = segments.toArray(new GCLogFileSegment[0]);
        Arrays.sort(workingList, Comparator.comparingInt(GCLogFileSegment::getSegmentIndex));

        int current = workingList.length;
        while (0 <= --current) {
            if (workingList[current].isCurrent()) {
                break;
            }
        }

        // if current == -1, then there is no current! What to do?
        if (current == -1) {
            Collections.addAll(segments, workingList);
            return segments;
        }

        double closestTime = Double.MAX_VALUE;
        // Find where current belongs.
        for (int index = current - 1; 0 <= index; --index) {
            double delta = GCLogFileSegment.rolloverDelta(workingList[current], workingList[index]);
            if (!Double.isNaN(delta) && 0 <= delta && delta < closestTime) {
                GCLogFileSegment temp = workingList[current];
                for (int n = current; n > index + 1; --n) {
                    workingList[n] = workingList[n - 1];
                }
                workingList[current = index + 1] = temp;
                closestTime = delta;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        Path path = new File("../gclogs/rolling/jdk14/rollinglogs/rollover.log").toPath();
        RotatingLogFileMetadata data = new RotatingLogFileMetadata(path);
        System.out.println(data.toString());
        path = new File("../gclogs/rolling/jdk14/rollinglogs/rollover.log.0").toPath();
        data = new RotatingLogFileMetadata(path);
        System.out.println(data.toString());
    }

}
