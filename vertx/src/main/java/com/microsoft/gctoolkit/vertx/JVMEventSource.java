// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.io.DataSource;
import com.microsoft.gctoolkit.parser.io.SafepointLogFile;
import com.microsoft.gctoolkit.vertx.internal.util.concurrent.StartingGun;
import io.vertx.core.AbstractVerticle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class JVMEventSource extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(JVMEventSource.class.getName());

    private final String publicationChannel;

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

    private final StartingGun deployed = new StartingGun();

    public void awaitDeployment() {
        deployed.awaitUninterruptibly();
    }

    public void start() {
        deployed.ready();
    }
}
