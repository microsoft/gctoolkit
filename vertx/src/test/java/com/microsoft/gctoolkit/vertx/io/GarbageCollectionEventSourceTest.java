// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.io;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.generational.DefNew;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.JVMEventSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class GarbageCollectionEventSourceTest {

    private static final String TEST_CHANNEL = "TEST";
    private static final String END_OF_DATA_SENTINEL = GCLogFile.END_OF_DATA_SENTINEL;

    private GCLogFile loadLogFile(Path path, boolean rotating) throws IOException {
        return rotating ? new RotatingGCLogFile(path) : new SingleGCLogFile(path);
    }
    
    @Test
    public void testRotatingLogDirectory() {
        Path path = new TestLogFile("rotating_directory").getFile().toPath();
        try {
            assertExpectedLineCountInLog(72210, loadLogFile(path, true));
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    public void testPlainTextFileLineCount() {
        Path path = new TestLogFile("streaming/gc.log").getFile().toPath();
        assertExpectedLineCountInLog(431604, new SingleGCLogFile(path));
    }

    @Test
    public void testGZipTarFileLineCount() {
        try {
            Path path = new TestLogFile("streaming/gc.log.tar.gz").getFile().toPath();
            assertExpectedLineCountInLog(410055, loadLogFile(path, false));
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testSingleLogInZipLineCount() {
        try {
            Path path = new TestLogFile("streaming/gc.log.zip").getFile().toPath();
            assertExpectedLineCountInLog(431604, loadLogFile(path, false));
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testRotatingLogsLineCount() {
        try {
            Path path = new TestLogFile("rotating.zip").getFile().toPath();
            assertExpectedLineCountInLog(72210, loadLogFile(path, true));
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    @Test
    public void testRotatingLogsRotatingLineCount() {
        try {
            Path path = new TestLogFile("rotating.zip").getFile().toPath();
            assertExpectedLineCountInLog(72210, loadLogFile(path, true));
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    /*
    72209 lines + EOF sentinal.
     */
    @Test
    public void testZippedDirectoryWithRotatingLogRotatingLineCount() {
        try {
            Path path = new TestLogFile("streaming/rotating_directory.zip").getFile().toPath();
            assertExpectedLineCountInLog(72209+1, loadLogFile(path, true));
        } catch (IOException ioe) {
            fail(ioe);
        }
    }

    private static void disableCaching() {
        System.setProperty("vertx.disableFileCPResolving", "true");
        System.setProperty("vertx.disableFileCaching", "true");
    }

    private void assertExpectedLineCountInLog(int expectedNumberOfLines, GCLogFile logFile) {
        CountDownLatch consumerStarted = new CountDownLatch(2);
        disableCaching();
        Vertx vertx = Vertx.vertx();

        GCLogConsumer consumer = new GCLogConsumer();
        vertx.deployVerticle(consumer, asyncResult -> consumerStarted.countDown());

        JVMEventSource garbageCollectionLogSource = new JVMEventSource(TEST_CHANNEL);
        vertx.deployVerticle(garbageCollectionLogSource, asyncResult -> consumerStarted.countDown());

        try {
            consumerStarted.await();
            garbageCollectionLogSource.publishGCDataSource(logFile);
            consumer.awaitEOF();
            vertx.undeploy(garbageCollectionLogSource.deploymentID());
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        assertEquals(expectedNumberOfLines, consumer.getEventCount());
    }

    private static class GCLogConsumer extends AbstractVerticle {

        private final CountDownLatch eof = new CountDownLatch(1);
        private int eventCount = 0;

        GCLogConsumer() {
        }

        public void awaitEOF() {
            try {
                eof.await();
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        }

        @Override
        public void start() {
            vertx.eventBus().consumer(TEST_CHANNEL, message -> {
                eventCount++;
                if (message.body().toString().equals(END_OF_DATA_SENTINEL))
                    eof.countDown();
            });
        }

        int getEventCount() {
            return eventCount;
        }
    }

    @Test
    public void testEqualsForDifferentObject() {
        GCEvent gcEvent = new DefNew(new DateTimeStamp("2018-04-04T09:10:00.586-0100"), GCCause.WARMUP,102);
        assertNotEquals(gcEvent, new ArrayList<>());
    }
}
