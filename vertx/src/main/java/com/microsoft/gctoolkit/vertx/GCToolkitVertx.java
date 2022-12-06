// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.jvm.JVMTermination;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.DataSourceChannel;
import com.microsoft.gctoolkit.message.DataSourceParser;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.vertx.io.JVMEventCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

import java.util.logging.Logger;

public class GCToolkitVertx extends AbstractVerticle implements DataSourceChannel {

    private static final Logger LOGGER = Logger.getLogger(GCToolkitVertx.class.getName());

    static {
        disableCaching();
    }

    public static void disableCaching() {
        System.setProperty("vertx.disableFileCPResolving", "true");
        System.setProperty("vertx.disableFileCaching", "true");
    }

    private final String mailBox;
    private final Vertx vertx;
    private DateTimeStamp timeOfLastEvent = new DateTimeStamp(0.0d);

    public GCToolkitVertx() {
        this("");
    }

    private GCToolkitVertx(String mailBox) {
        disableCaching();
        this.mailBox = mailBox;
        this.vertx = Vertx.vertx();
        vertx.eventBus().registerDefaultCodec(JVMEvent.class, new JVMEventCodec());
    }

//    /**
//     * Parse the data source and feed events to the aggregators.
//     * @param dataSource The JVM event data source
//     * @param logFileParsers The parsers for parsing the data source
//     * @param aggregatorVerticles The verticles that dispatch events to the aggregators
//     * @param mailBox The mailbox for the log file parser.
//     * @return The runtime duration
//     * @throws IOException If an IOException is thrown while reading the DataSource
//     */
//    public static DateTimeStamp aggregateDataSource(DataSource<?> dataSource, Set<LogFileParser> logFileParsers, Set<AggregatorVerticle> aggregatorVerticles,  String mailBox) throws IOException {
//    	//remove AggregatorVerticle which can not match by the LogFileParser to prevent dead loop
//        aggregatorVerticles.removeIf(aggregatorVerticle->{
//            boolean isMatch = logFileParsers.stream().map(LogFileParser::getOutbox)
//                    .anyMatch(outbox->outbox.equals(aggregatorVerticle.getInbox()));
//            if(!isMatch)
//                LOGGER.log(Level.SEVERE, String.format("Remove %s %s",aggregatorVerticle.getInbox(),aggregatorVerticle));
//            return !isMatch;
//        });
//
//        GCToolkitVertx gcToolkitVertx = new GCToolkitVertx(mailBox);
//        JVMEventSource jvmEventSource = new JVMEventSource();
//        gcToolkitVertx.deployVerticle(jvmEventSource);
//        jvmEventSource.awaitDeployment();
//
//        gcToolkitVertx.deployVerticle(gcToolkitVertx);
//
//        logFileParsers.forEach(logFileParser -> gcToolkitVertx.deployVerticle(logFileParser, new DeploymentOptions().setWorker(true)));
//        logFileParsers.forEach(LogFileParser::awaitDeployment);
//
//        aggregatorVerticles.forEach(gcToolkitVertx::deployVerticle);
//        aggregatorVerticles.forEach(AggregatorVerticle::awaitDeployment);
//
//        jvmEventSource.publishGCDataSource(dataSource);
//        aggregatorVerticles.forEach(AggregatorVerticle::awaitCompletion);
//
//
//        return gcToolkitVertx.timeOfLastEvent;
//    }

    private Future<String> deployVerticle(Verticle verticle) {
        return vertx.deployVerticle(verticle);
    }

    private Future<String> deployVerticle(Verticle verticle, DeploymentOptions deploymentOptions) {
        return vertx.deployVerticle(verticle, deploymentOptions);
    }

    @Override
    public void start() {
        try {
            vertx.eventBus().
                    consumer(mailBox, message -> {
                        try {
                            JVMEvent event = (JVMEvent) message.body();
                            DateTimeStamp now = event.getDateTimeStamp().add(event.getDuration());
                            if (now.after(timeOfLastEvent)) {
                                timeOfLastEvent = now;
                            }
                            if (event instanceof JVMTermination)
                                return;
                        } catch (Throwable t) {
                            LOGGER.throwing(this.getClass().getName(), "start", t);
                        }
                    });
        } catch (Throwable t) {
            LOGGER.throwing(this.getClass().getName(), "start", t);
        }
    }

    @Override
    public void registerListener(DataSourceParser listener) {

    }

    @Override
    public void publish(Channels channel, String message) {

    }
}
