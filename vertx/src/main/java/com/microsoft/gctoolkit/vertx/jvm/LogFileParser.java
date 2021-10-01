// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.vertx.internal.util.concurrent.StartingGun;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import com.microsoft.gctoolkit.parser.GCLogParser;
import com.microsoft.gctoolkit.parser.JVMEventConsumer;

import java.util.logging.Level;
import java.util.logging.Logger;


public class LogFileParser extends AbstractVerticle implements JVMEventConsumer {

    protected static final Logger LOGGER = Logger.getLogger(LogFileParser.class.getName());
    private final GCLogParser parser;

    private final String inbox;
    private final String outbox;

    public LogFileParser(String inbox, String outbox, ParserFactory factory) {
        this.inbox = inbox;
        this.outbox = outbox;
        parser = factory.get(this);
    }

    private final DeliveryOptions options = new DeliveryOptions().setCodecName("JVMEvent");

    public void record(JVMEvent event) {
        try {
            if (event != null && event.getDateTimeStamp() != null) {
                vertx.eventBus().publish(outbox, event, options);
            } else {
                LOGGER.log(Level.SEVERE, "Thread: {0} is recording Event: {1} that has a null DateTimeStamp, it will be ignored", new Object[]{Thread.currentThread().getName(), event.toString()});
            }
        } catch (Error t) {
            LOGGER.throwing(this.getClass().toString(), "record", t);
        }
    }

    //Vert.x
    private final StartingGun deployed = new StartingGun();

    public void awaitDeployment() {
        deployed.awaitUninterruptibly();
    }

    @Override
    public void start() {
        vertx.eventBus().
                consumer(inbox, message -> {
                    try {
                        String body = ((String) message.body()).trim();
                        if (body.isEmpty()) return;
                        parser.receive(body);
                    } catch (Throwable t) {
                        LOGGER.throwing(this.getClass().getName(), "start", t);
                    }
                });
        deployed.ready();
    }
}
