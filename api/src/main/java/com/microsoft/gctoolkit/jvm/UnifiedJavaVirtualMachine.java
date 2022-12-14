// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of JavaVirtualMachine that uses io.vertx verticles to feed
 * lines to the parser(s) and post events to the aggregators. This implementation
 * is here in the vertx module so that the api and parser modules can exist without
 * having to import io.vertx. In the api module, the class GCToolKit uses the classloader
 * to load UnifiedJavaVirtualMachine.
 */
public class UnifiedJavaVirtualMachine extends AbstractJavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(UnifiedJavaVirtualMachine.class.getName());

    @Override
    public boolean accepts(DataSource logFile) {
        try {
            if (((GCLogFile) logFile).isUnified()) {
                super.setDataSource(logFile);
                return true;
            }
        } catch(IOException ioe) {
            LOGGER.log(Level.WARNING, ioe.getMessage());
        }
        return false;
    }

    @Override
    public boolean isUnifiedLogging() {
        return true;
    }
}
