// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.patterns;


import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.parser.GCLogParser;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ParserTest {

    private Diarizer diarizer;
    private GCLogParser parser;

    @BeforeEach
    public void setUp() {
        parser = parser();
        diarizer = diarizer();
    }

    /**
     * The test provides an instance of a Diarizer appropriate to the gc log or lines being parsed.
     * @return A Diarizer appropriate to the gc log or lines being parsed
     */
    protected abstract Diarizer diarizer();

    /**
     * The test provides an instance of a GCLogParser appropriate to the gc log or lines being parsed.
     * @return A GCLogParser appropriate to the gc log or lines being parsed
     */
    protected abstract GCLogParser parser();

    /**
     * Parser runs in its own thread so start one for it and then feed it the lines to be parsed
     *
     * @param lines
     * @return
     */
    protected List<JVMEvent> feedParser(String[] lines) {
        Diarizer diarizer = diarizer();
        GCLogParser parser = parser();

        Arrays.stream(lines).forEach(diarizer::diarize);
        parser.diary(diarizer.getDiary());

        ParserTestSupportChannel  channel = new ParserTestSupportChannel();
        parser.publishTo(channel);

        Arrays.stream(lines).map(String::trim).forEach(parser::receive);

        return channel.events();
    }

    /**
     * Common check for all GC events that report on memory.
     *
     * @param summary
     * @param occupancyAtStartOfCollection
     * @param sizeAtStartOfCollection
     * @param occupancyAfterCollection
     * @param sizeAfterCollection
     */
    protected void assertMemoryPoolValues(MemoryPoolSummary summary, long occupancyAtStartOfCollection, long sizeAtStartOfCollection, long occupancyAfterCollection, long sizeAfterCollection) {
        assertEquals(summary.getOccupancyBeforeCollection(), occupancyAtStartOfCollection);
        assertEquals(summary.getSizeBeforeCollection(), sizeAtStartOfCollection);
        assertEquals(summary.getOccupancyAfterCollection(), occupancyAfterCollection);
        assertEquals(summary.getSizeAfterCollection(), sizeAfterCollection);
    }

    public class ParserTestSupportChannel implements JVMEventChannel {

        final List<JVMEvent> events = new ArrayList<>();
        ParserTestSupportChannel() {}

        @Override
        public void registerListener(JVMEventChannelListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void publish(Channels channel, JVMEvent message) {
            events.add(message);
        }

        public List<JVMEvent> events() {
            return events;
        }
    }
}
