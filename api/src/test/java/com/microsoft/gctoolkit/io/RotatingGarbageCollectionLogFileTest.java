// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RotatingGarbageCollectionLogFileTest {

    private String[][] expected = new String[][] {
            new String[] {
                    "rolling/jdk14/rollinglogs/long_restart.log",
                    "long_restart.log.3", "long_restart.log.4", "long_restart.log.0", "long_restart.log.1", "long_restart.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/restart_after_rollover.log",
                    "restart_after_rollover.log.4", "restart_after_rollover.log.0", "restart_after_rollover.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/rollover.log",
                    "rollover.log.3", "rollover.log.4", "rollover.log.0", "rollover.log.1", "rollover.log.2", "rollover.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/short_restart_2.log",
                    "short_restart_2.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/short_restart_3.log",
                    "short_restart_3.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/short_restart_4.log",
                    "short_restart_4.log"
            },
            new String[] {
                    "rolling/jdk14/rollinglogs/singleseries.log",
                    "singleseries.log.1", "singleseries.log.2", "singleseries.log"
            }
    };

    @Test
    public void getOrderedGarbageCollectionLogFiles() {
        for(String[] data : expected) {
            try {
                RotatingGCLogFile garbageCollectionLogFile = createRotatingGarbageCollectionLogFile(data[0]);
                List<GarbageCollectionLogFileSegment> segments = garbageCollectionLogFile.getOrderedGarbageCollectionLogFiles();
                assertEquals(data.length - 1, segments.size());
                for (int n = 1; n < data.length; n++) {
                    assertEquals(data[n], segments.get(n - 1).getPath().getFileName().toString());
                }
            } catch (IOException e) {
                fail(e);
            }
        }
    }

    @Test
    public void testRollingLogOrderUsage() {
        Path path = getPath( "rolling/jdk14/rollinglogs/rollover.log");
        List<String> expected = Arrays.asList(
                "rollover.log.3", "rollover.log.4", "rollover.log.0", "rollover.log.1", "rollover.log.2", "rollover.log"
        );
        try {
            RotatingGCLogFile rotatingGCLogFile = new RotatingGCLogFile(path);
            List<String> actual =
                    rotatingGCLogFile
                            .getOrderedGarbageCollectionLogFiles()
                            .stream()
                            .map(segment -> segment.getPath().getFileName().toString())
                            .collect(Collectors.toList());
            assertEquals(expected, actual);
        } catch (Exception badTestData) {
            fail(badTestData);
        }
    }

    static RotatingGCLogFile createRotatingGarbageCollectionLogFile(String partialPath) throws IOException {
        Path path = getPath(partialPath);
        List<GarbageCollectionLogFileSegment> segments = getSegments(path);
        return new RotatingGCLogFile(path.getParent(), segments);
    }

    static final String[] roots = new String[] {
            "./gclogs/",
            "../gclogs/",
            "../../gclogs/"
    };

    static Path getPath(String filename) {
        return Arrays.stream(roots)
                .map(path -> Paths.get(path, filename))
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(filename + " not found"));
    }

    static List<GarbageCollectionLogFileSegment> getSegments(Path path) {
        Path directory = path.getParent();
        String filename = path.getFileName().toString();
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:"+filename+"*");
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(p -> pathMatcher.matches(p.getFileName()))
                    .map(GarbageCollectionLogFileSegment::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            // autoclose...
            return List.of();
        }
    }
}
