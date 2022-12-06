// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.vertx.internal.util.concurrent.StartingGun;
import io.vertx.core.AbstractVerticle;

import java.io.IOException;

public class JVMEventSource extends AbstractVerticle implements JVMEventChannel {

    public JVMEventSource() {}

    public void publishGCDataSource(DataSource<?> dataSource) throws IOException {
//        dataSource.stream().forEach(entry -> vertx.eventBus().publish(publicationChannel, entry));
//        vertx.eventBus().publish(publicationChannel, dataSource.endOfData());
    }
    // Safepoint details are in a separate log prior to unified logging. todo: rewire safepoint details for JDK 8
//    public void publishSafePointLogFile(Path logFile) throws IOException {
//        SafepointLogFile safepointLogFile = new SafepointLogFile(logFile);
//        safepointLogFile.stream().forEach(entry -> vertx.eventBus().publish(publicationChannel, entry));
//        vertx.eventBus().publish(publicationChannel, safepointLogFile.endOfData());
//    }

    private final StartingGun deployed = new StartingGun(); //todo: replace with a future

    public void awaitDeployment() {
        deployed.awaitUninterruptibly();
    }


    // data source bus methods

    public void start() {
        deployed.ready();
    }

    @Override
    public void registerListener(JVMEventChannelListener listener) {

    }

    @Override
    public void publish(Channels channel, JVMEvent message) {

    }

}
