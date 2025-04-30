// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;


import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.SurvivorMemoryPoolSummary;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.message.ChannelName;
import com.microsoft.gctoolkit.message.JVMEventChannel;
import com.microsoft.gctoolkit.message.JVMEventChannelListener;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class ParserTest {

    private Diarizer diarizer;
    private GCLogParser parser;
    private ParserTestSupportChannel  channel;

    @BeforeEach
    public void setUp() {
        channel = new ParserTestSupportChannel();
        diarizer = diarizer();
        parser = parser();
        parser.publishTo(channel);
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

    GCLogParser getParser() {
        return this.parser;
    }

    /**
     * Parser runs in its own thread so start one for it and then feed it the lines to be parsed
     *
     * @param lines The GC log lines to be fed to the parser.
     * @return The list of JVMEvents from the parsed lines.
     */
    protected List<JVMEvent> feedParser(String[] lines) {
        Arrays.stream(lines).forEach(diarizer::diarize);
        parser.diary(diarizer.getDiary());
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

    /**
     * Common check for GC events that report on Survivor memory.
     *
     * @param summary
     * @param occupancyAtStartOfCollection
     * @param sizeAtStartOfCollection
     * @param occupancyAfterCollection
     * @param sizeAfterCollection
     */
    protected void assertSurvivorMemoryPoolValues(SurvivorMemoryPoolSummary summary, long occupancyAtStartOfCollection, long occupancyAfterCollection) {
        assertEquals(summary.getOccupancyBeforeCollection(), occupancyAtStartOfCollection);
        assertEquals(summary.getOccupancyAfterCollection(), occupancyAfterCollection);
    }
        
    /**
     * Common check for GC events that report on CPU usage
     * 
     * @param summary
     * @param user
     * @param sys  (kernel)
     * @param real (wallClock)
     */
    protected void assertCPUSummaryValues(CPUSummary summary, double user, double sys, double real) {
    	assertNotNull(summary);
    	assertDoubleEquals(summary.getUser(), user);
    	assertDoubleEquals(summary.getKernel(), sys);
    	assertDoubleEquals(summary.getWallClock(), real);
    }
    
    /**
     * Compare doubles using a default epsilon value of 0.0000001, which should be
     * sufficient for most duration representations.
     * 
     * @param d1
     * @param d2
     */
    protected void assertDoubleEquals(double d1, double d2) {
    	double defaultEpsilon = 0.0000001d;
    	assertDoubleEquals(d1, d2, defaultEpsilon);
    }
    
    /**
     * Compare doubles without worrying about binary representation discrepancies
     *  
     * @param d1
     * @param d2
     * @param epsilon
     */
    protected void assertDoubleEquals(double d1, double d2, double epsilon) {
    	assertTrue(Math.abs(d1-d2) < epsilon);
    }
    
    public class ParserTestSupportChannel implements JVMEventChannel {

        final List<JVMEvent> events = new ArrayList<>();
        ParserTestSupportChannel() {}

        @Override
        public void registerListener(JVMEventChannelListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void publish(ChannelName channel, JVMEvent message) {
            events.add(message);
        }

        /**
         * Unfortunately needs to be implemented but doesn't need to do anything
         */
        @Override
        public void close() {

        }

        public List<JVMEvent> events() {
            return events;
        }
    }
}
