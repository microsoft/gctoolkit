// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;

import java.util.Set;
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
    public boolean accepts(GCLogFile logFile) {
        return logFile.isUnified();
    }

    @Override
    GCToolkitVertxParameters getParameters(Set<Class<? extends Aggregation>> registeredAggregations, Diary diary) {
        return new GCToolkitVertxParametersForUnifiedLogs(registeredAggregations, diary);
    }

}
