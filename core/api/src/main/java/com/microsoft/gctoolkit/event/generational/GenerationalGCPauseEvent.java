// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public abstract class GenerationalGCPauseEvent extends GenerationalGCEvent {

    private MemoryPoolSummary young;
    private MemoryPoolSummary tenured;
    private MemoryPoolSummary heap;
    private MemoryPoolSummary permOrMetaspace;
    private MemoryPoolSummary nonClassspace;
    private MemoryPoolSummary classspace;
    private ReferenceGCSummary referenceGCSummary;
    private double classUnloadingProcessingTime;
    private double symbolTableProcessingTime;
    private double stringTableProcessingTime;
    private double symbolAndStringTableProcessingTime;
    private BinaryTreeDictionary binaryTreeDictionary;

    private CPUSummary cpuSummary;

    public GenerationalGCPauseEvent(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double duration) {
        super(timeStamp, type, cause, duration);
    }

    public void add(MemoryPoolSummary heap) {
        this.heap = heap;
    }

    public void add(MemoryPoolSummary young, MemoryPoolSummary heap) {
        this.add(
                young,
                heap.minus(young),
                heap);
    }

    public void add(MemoryPoolSummary young, MemoryPoolSummary tenured, MemoryPoolSummary heap) {
        this.young = young;
        this.tenured = tenured;
        this.heap = heap;
    }

    public void addPermOrMetaSpaceRecord(MemoryPoolSummary permOrMetaspaceRecord) {
        permOrMetaspace = permOrMetaspaceRecord;
    }

    public void addClassspace(MemoryPoolSummary pool) {
        classspace = pool;
    }

    public void addNonClassspace(MemoryPoolSummary pool) {
        nonClassspace = pool;
    }

    public void add(ReferenceGCSummary referenceGCSummary) {
        this.referenceGCSummary = referenceGCSummary;
    }

    public void add(CPUSummary summary) {
        this.cpuSummary = summary;
    }

    public MemoryPoolSummary getYoung() {
        return this.young;
    }

    public MemoryPoolSummary getTenured() {
        return this.tenured;
    }

    public MemoryPoolSummary getHeap() {
        return this.heap;
    }

    public MemoryPoolSummary getPermOrMetaspace() {
        return this.permOrMetaspace;
    }

    public MemoryPoolSummary getNonClassspace() {
        return this.nonClassspace;
    }

    public MemoryPoolSummary getClassspace() {
        return this.classspace;
    }

    public ReferenceGCSummary getReferenceGCSummary() {
        return this.referenceGCSummary;
    }

    public CPUSummary getCpuSummary() {
        return this.cpuSummary;
    }

    public void addReferenceGCSummary(ReferenceGCSummary summary) {
        this.referenceGCSummary = summary;
    }

    /*
     * weak, class unloading, symbol table, string table, symbol and string
     * combined as reference processing table
     */
    public void addClassUnloadingAndStringTableProcessingDurations(double classUnloading, double symbolTable, double stringTable, double symbolAndStringTable) {
        this.classUnloadingProcessingTime = classUnloading;
        this.symbolTableProcessingTime = symbolTable;
        this.stringTableProcessingTime = stringTable;
        this.symbolAndStringTableProcessingTime = symbolAndStringTable;
    }

    public double getClassUnloadingProcessingTime() {
        return this.classUnloadingProcessingTime;
    }

    public double getSymbolTableProcessingTime() {
        return this.symbolTableProcessingTime;
    }

    public double getStringTableProcessingTime() {
        return this.stringTableProcessingTime;
    }

    public double getSymbolAndStringTableProcessingTime() {
        return this.symbolAndStringTableProcessingTime;
    }

    public void addBinaryTreeDictionary(BinaryTreeDictionary dictionary) {
        this.binaryTreeDictionary = dictionary;
    }

    public BinaryTreeDictionary getBinaryTreeDictionary() {
        return this.binaryTreeDictionary;
    }

}
