// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.SurvivorMemoryPoolSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * G1 Event to capture parser errors
 */

public class G1Trap extends G1GCPauseEvent {

    private static final Logger LOGGER = Logger.getLogger(G1Trap.class.getName());

    private static final String message = "Internal EventSource Error @ ";

    public G1Trap() {
        super(new DateTimeStamp(0.0d), GarbageCollectionTypes.G1Trap, GCCause.UNKNOWN_GCCAUSE, 0.0d);
    }

    private int errorCount = 0;

    private void trap(Exception e) {
        if (errorCount > 100) return;
        errorCount++;
        LOGGER.log(Level.INFO, message + super.getDateTimeStamp().toString());
    }

    @Override
    public DateTimeStamp getDateTimeStamp() {
        trap(new Exception());
        return super.getDateTimeStamp();
    }

    @Override
    public double getDuration() {
        trap(new Exception());
        return super.getDuration();
    }

    @Override
    public String toString() {
        return "GCTrap: " + super.toString();
    }

    @Override
    public void setGCCause(GCCause cause) {
        trap(new Exception());
    }

    @Override
    public GCCause getGCCause() {
        trap(new Exception());
        return super.getGCCause();
    }

    @Override
    public GarbageCollectionTypes getGarbageCollectionType() {
        trap(new Exception());
        return super.getGarbageCollectionType();
    }

    @Override
    public void addMemorySummary(MemoryPoolSummary eden, SurvivorMemoryPoolSummary survivor, MemoryPoolSummary heap) {
        trap(new Exception());
    }

    @Override
    public void addCPUSummary(CPUSummary summary) {
        trap(new Exception());
    }

    @Override
    public MemoryPoolSummary getEden() {
        trap(new Exception());
        return super.getEden();
    }

    @Override
    public SurvivorMemoryPoolSummary getSurvivor() {
        trap(new Exception());
        return super.getSurvivor();
    }

    @Override
    public MemoryPoolSummary getHeap() {
        trap(new Exception());
        return super.getHeap();
    }

    @Override
    public CPUSummary getCpuSummary() {
        trap(new Exception());
        return super.getCpuSummary();
    }


    public void addCPUSummary() {
        trap(new Exception());
    }

    public void execute(Aggregator<?> aggregator) {
    }
}
