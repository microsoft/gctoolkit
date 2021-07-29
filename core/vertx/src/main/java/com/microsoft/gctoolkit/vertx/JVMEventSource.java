// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.io.DataSource;
import io.vertx.core.AbstractVerticle;
import com.microsoft.gctoolkit.parser.io.SafepointLogFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class JVMEventSource extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(JVMEventSource.class.getName());

    private String publicationChannel;

    public JVMEventSource(String publicationChannel) {
        this.publicationChannel = publicationChannel;
    }

    public void publishGCDataSource(DataSource<?> dataSource) throws IOException {
        dataSource.stream().forEach(entry -> vertx.eventBus().publish(publicationChannel, entry));
        vertx.eventBus().publish(publicationChannel, dataSource.endOfData());
    }

    public void publishSafePointLogFile(Path logFile) throws IOException {
        SafepointLogFile safepointLogFile = new SafepointLogFile(logFile);
        safepointLogFile.stream().forEach(entry -> vertx.eventBus().publish(publicationChannel, entry));
        vertx.eventBus().publish(publicationChannel, safepointLogFile.endOfData());
    }

    CountDownLatch latch = new CountDownLatch(1);

    public void awaitDeployment() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            LOGGER.throwing("JVMEventSource", "awaitDeployment", e);
        }
    }

    public void start() {
        latch.countDown();
    }
}
