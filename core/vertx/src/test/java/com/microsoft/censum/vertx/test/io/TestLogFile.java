// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.test.io;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * Abstract the log files location and provide some parsing of the file name scheme.
 */
public class TestLogFile {

    private File logFile;

    private final String[] relativePaths = {
            "./",
            "preunified/",
            "preunified/cms/parnew/details/tenuring/",
            "preunified/verbose/tenuring/",
            "preunified/ps/details/tenuring/",
            "preunified/ps/details/",
            "unified/",
            "streaming/",
            "safepoint/",
    };

    public TestLogFile(String fileName) {
        Optional<File> fileOptional = Arrays.stream(relativePaths)
                .flatMap(path -> Arrays.stream(
                        new String[]{
                                "./" + path,
                                "../" + path,
                                "../../" + path,
                                "./gclogs/" + path,
                                "../gclogs/" + path,
                                "../../gclogs/" + path,
                        }
                )).
                        map(path -> new File(path + File.separator + fileName)).
                        filter(file -> file.exists())
                .findFirst();

        if (!fileOptional.isPresent()) {
            throw new RuntimeException(fileName + " not found");
        } else {
            logFile = fileOptional.get();
        }

    }

    public TestLogFile(File file) {
        this.logFile = file;
    }

    public String getPath() {
        return logFile.getPath();
    }

    public File getFile() {
        return logFile;
    }

}
