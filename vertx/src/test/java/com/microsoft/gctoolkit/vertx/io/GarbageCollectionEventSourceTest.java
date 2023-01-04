// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.io;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.generational.DefNew;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.RotatingGCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceParser;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.VertxDataSourceChannel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class GarbageCollectionEventSourceTest {

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
        //CountDownLatch consumerStarted = new CountDownLatch(2);
        disableCaching();

        GCLogConsumer consumer = new GCLogConsumer();
        VertxDataSourceChannel channel = new VertxDataSourceChannel();
        channel.registerListener(consumer);
        //vertx.deployVerticle(consumer, asyncResult -> consumerStarted.countDown());

        //todo: this just completely broke this test :-/ JVMEventSource garbageCollectionLogSource = new JVMEventSource(TEST_CHANNEL);
        //JVMEventSource garbageCollectionLogSource = new JVMEventSource();
        //vertx.deployVerticle(garbageCollectionLogSource, asyncResult -> consumerStarted.countDown());

        try {
            logFile.stream().forEach(message -> {
                channel.publish(Channels.DATA_SOURCE, message);
            });
            channel.publish(Channels.DATA_SOURCE, END_OF_DATA_SENTINEL);
            //consumerStarted.await();
            consumer.awaitEOF();
            //vertx.undeploy(garbageCollectionLogSource.deploymentID());
        } catch (IOException e) {
        //} catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        assertEquals(expectedNumberOfLines, consumer.getEventCount());
    }

    private static class GCLogConsumer implements DataSourceParser {

        private final CountDownLatch eof = new CountDownLatch(1);
        private int eventCount = 0;

        @Override
        public Channels channel() {
            return Channels.DATA_SOURCE;
        }

        @Override
        public void receive(String payload) {
            eventCount++;
            if ( END_OF_DATA_SENTINEL.equals(payload))
                eof.countDown();
        }

        GCLogConsumer() {
        }

        public void awaitEOF() {
            try {
                eof.await();
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        }

        int getEventCount() {
            return eventCount;
        }

        @Override
        public void publishTo(JVMEventChannel channel) {
            throw new IllegalStateException();
        }

        @Override
        public void diary(Diary diary) {
            throw new IllegalStateException();
        }

        @Override
        public boolean accepts(Diary diary) {
            return false;
        }
    }

    @Test
    public void testEqualsForDifferentObject() {
        GCEvent gcEvent = new DefNew(new DateTimeStamp("2018-04-04T09:10:00.586-0100"), GCCause.WARMUP,102);
        assertNotEquals(gcEvent, new ArrayList<>());
    }
}
