// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.CPUSummary;
import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.event.MemoryPoolSummary;
import com.microsoft.censum.event.ReferenceGCSummary;
import com.microsoft.censum.time.DateTimeStamp;

public abstract class GenerationalGCPauseEvent extends GenerationalGCEvent {

    MemoryPoolSummary young;
    MemoryPoolSummary tenured;
    MemoryPoolSummary heap;
    MemoryPoolSummary permOrMetaspace;
    MemoryPoolSummary nonClassspace;
    MemoryPoolSummary classspace;
    ReferenceGCSummary referenceGCSummary;
    double classUnloadingProcessingTime;
    double symbolTableProcessingTime;
    double stringTableProcessingTime;
    double symbolAndStringTableProcessingTime;
    BinaryTreeDictionary binaryTreeDictionary;

    CPUSummary cpuSummary;

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
