// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.patterns;


import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.message.Channels;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import com.microsoft.gctoolkit.parser.GCLogParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {

    protected ParserTestSupportChannel setup(GCLogParser parser, Diary diary) {
        ParserTestSupportChannel  channel = new ParserTestSupportChannel();
        parser.publishTo(channel);
        parser.diary(diary);
        return channel;
    }


    /**
     * Parser runs in it's own thread so start one for it and then feed it the lines to be parsed
     *
     * @param parser
     * @param lines
     */
    protected void feedParser(GCLogParser parser, String[] lines) {
        Arrays.stream(lines).map(String::trim).forEach(parser::receive);
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
