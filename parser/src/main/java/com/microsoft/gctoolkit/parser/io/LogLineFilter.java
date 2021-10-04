// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.io;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogLineFilter {

    private static final Logger LOGGER = Logger.getLogger(LogLineFilter.class.getName());

    //Define a regex prefix to remove from the logging statement
    //Example, -Dcom.microsoft.gctoolkit.filter="^\d{8}\.\d{6} strips off an 8 digit dot 6 digit number off of the front of the line.
    private static final String PREFIX_FILTER_PROPERTY = "com.microsoft.gctoolkit.filter"; //Define a prefix to remove from the logging statement

    private static final String VERBOSE_PROPERTY = "com.microsoft.gctoolkit.verbose.log";
    private static boolean verbose = false;

    //Internal hack to deal with our sftp server not being able to capture a complete zip file.
    private static String prefixFilter = null;
    private Pattern filterPattern = null;

    static {
        prefixFilter = System.getProperty(PREFIX_FILTER_PROPERTY);
        verbose = Boolean.getBoolean(VERBOSE_PROPERTY);
    }

    public LogLineFilter() {
        if (prefixFilter != null) {
            if (!prefixFilter.startsWith("^"))
                prefixFilter = "^" + prefixFilter;
            filterPattern = Pattern.compile(prefixFilter + "(.+)");
        }
    }

    /**
     * Some clients store logs with a prefix. Filter the prefix out of the log entry prior to parsing
     *
     * @param line A line from the GC log file.
     * @return The same line without the prefix.
     */
    public String prefixFilter(String line) {
        if (verbose)
            LOGGER.fine(line);
        if (filterPattern == null)
            return line;
        try {
            Matcher matcher = filterPattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, t.getMessage(), LOGGER);
        }

        return line;
    }
}

