// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.jvm.MetaspaceRecord;
import com.microsoft.gctoolkit.parser.jvm.Decorators;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.ConcurrentModeFailure;
import static com.microsoft.gctoolkit.event.GarbageCollectionTypes.FullGC;

public class GenerationalForwardReference extends ForwardReference {

    private GarbageCollectionTypes gcType;
    private MemoryPoolSummary young = null;
    private MemoryPoolSummary tenured = null;
    private MemoryPoolSummary heap = null;
    private MetaspaceRecord metaspace = null;
    private MetaspaceRecord nonClassspace = null;

    public MetaspaceRecord getNonClassspace() {
        return nonClassspace;
    }

    public void setNonClassspace(MetaspaceRecord nonClassspace) {
        this.nonClassspace = nonClassspace;
    }

    public MetaspaceRecord getClassspace() {
        return classspace;
    }

    public void setClassspace(MetaspaceRecord classspace) {
        this.classspace = classspace;
    }

    private MetaspaceRecord classspace = null;
    private final Map<String, Double> remarkPhases = new ConcurrentHashMap<>();
    private final Map<String, Double> phases = new ConcurrentHashMap<>();

    public GenerationalForwardReference(GarbageCollectionTypes gcType, Decorators decorators, int gcid) {
        super(decorators, gcid);
        this.gcType = gcType;
    }

    GarbageCollectionTypes getGarbageCollectionType() {
        return this.gcType;
    }

    void setYoung(MemoryPoolSummary young) {
        this.young = young;
    }

    MemoryPoolSummary getYoung() {
        return this.young;
    }

    void setTenured(MemoryPoolSummary tenured) {
        this.tenured = tenured;
    }

    MemoryPoolSummary getTenured() {
        return this.tenured;
    }

    void setMetaspace(MetaspaceRecord metaSpaceRecord) {
        this.metaspace = metaSpaceRecord;
    }

    MetaspaceRecord getMetaspace() {
        return metaspace;
    }

    void setHeap(MemoryPoolSummary occupancyWithMemoryPoolSizeSummary) {
        this.heap = occupancyWithMemoryPoolSizeSummary;
    }

    MemoryPoolSummary getHeap() {
        return this.heap;
    }

    void addCMSRemarkPhase(String phase, double duration) {
        remarkPhases.put(phase, duration);
    }

    double getPhaseDuration(String phaseName) {
        return remarkPhases.get(phaseName);
    }

    Stream<String> remarkPhases() {
        return remarkPhases.keySet().stream();
    }

    public void convertToConcurrentModeFailure() {
        gcType = ConcurrentModeFailure;
    }

    public void convertToSerialFull() {
        gcType = FullGC;
    }

    public void addFullGCPhase(String phase, double duration) {
        phases.put(phase, duration);
    }
}
