// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.io.GCLogFile;

import java.util.Set;
import java.util.logging.Logger;

/**
 * An implementation of JavaVirtualMachine that uses io.vertx verticles to feed
 * lines to the parser(s) and post events to the aggregators. This implementation
 * is here in the vertx module so that the api and parser modules can exist without
 * having to import io.vertx. In the api module, the class GCToolKit uses the classloader
 * to load required JavaVirtualMachine.
 */
public class PreUnifiedJavaVirtualMachine extends AbstractJavaVirtualMachine {

    private static final Logger LOGGER = Logger.getLogger(PreUnifiedJavaVirtualMachine.class.getName());

    // Safepoint details are in a different log and would be loaded separately pre-unified.
    @Override
    public boolean accepts(DataSource logFile) {
        if ( logFile instanceof GCLogFile)
            return ! ((GCLogFile)logFile).isUnified();
        return false;
    }

//    @Override
//    GCToolkitVertxParameters getParameters(Set<Class<? extends Aggregation>> registeredAggregations, Diary diary) {
//        return new GCToolkitVertxParametersForPreUnifiedLogs(registeredAggregations, diary);
//    }

    @Override
    public boolean isZGC() {
        return false;
    }

    @Override
    public boolean isShenandoah() {
        return false;
    }

}
