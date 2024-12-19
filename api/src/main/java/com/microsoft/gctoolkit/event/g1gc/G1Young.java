// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.StatisticalSummary;
import com.microsoft.gctoolkit.event.UnifiedStatisticalSummary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;


public class G1Young extends G1RealPause {

    private double parallelPhaseDuration = -1.0d;
    private int gcWorkers;
    private int evacuationWorkersUsed;
    private int evacuationWorkersAvailable;
    private double codeRootFixupDuration = -1.0d;
    private double codeRootMigrationDuration = -1.0d;
    private double codeRootPurgeDuration = -1.0d;
    private double clearCTDuration = -1.0d;
    private double expandHeapDuration = -1.0d;
    private double stringDedupingDuration = -1.0d;
    private int stringDeduppingWorkers;
    private StatisticalSummary queueFixupStatistics;
    private StatisticalSummary tableFixupStatistics;
    private double otherPhaseDurations = -1.0d;
    private StatisticalSummary workersStart;
    private StatisticalSummary workersEnd;
    private StatisticalSummary workerOther;
    private StatisticalSummary workerTotal;
    private StatisticalSummary processedBuffersSummary;
    private boolean toSpaceExhausted = false;

    private final Map<String, StatisticalSummary> parallelPhaseSummaries = new ConcurrentHashMap<>();
    private final Map<String, Double> phaseDurations = new ConcurrentHashMap<>();

    public G1Young(DateTimeStamp dateTimeStamp, GarbageCollectionTypes gcType, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, gcType, gcCause, pauseTime);
    }

    public G1Young(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        this(dateTimeStamp, GarbageCollectionTypes.Young, gcCause, pauseTime);
    }

    public void toSpaceExhausted() {
        toSpaceExhausted = true;
    }

    public boolean isToSpaceExhausted() {
        return toSpaceExhausted;
    }

    public void setParallelPhaseDuration(double duration) {
        this.parallelPhaseDuration = duration;
    }

    public void setGcWorkers(int count) {
        this.gcWorkers = count;
    }

    public void setEvacuationWorkersUsed(int evacuationWorkersUsed) {
        this.evacuationWorkersUsed = evacuationWorkersUsed;
    }

    public void setEvacuationWorkersAvailable(int evacuationWorkersAvailable) {
        this.evacuationWorkersAvailable = evacuationWorkersAvailable;
    }

    public void setCodeRootFixupDuration(double duration) {
        this.codeRootFixupDuration = duration;
    }

    public void setStringDedupingDuration(double duration, int workers) {
        this.stringDedupingDuration = duration;
        this.stringDeduppingWorkers = workers;
    }

    public void setQueueFixupStatistics(StatisticalSummary summary) {
        this.queueFixupStatistics = summary;
    }

    public void setTableFixupStatistics(StatisticalSummary summary) {
        this.tableFixupStatistics = summary;
    }

    public void setCodeRootMigrationDuration(double duration) {
        this.codeRootMigrationDuration = duration;
    }

    public void setCodeRootPurgeDuration(double duration) {
        this.codeRootPurgeDuration = duration;
    }

    public void setClearCTDuration(double duration) {
        this.clearCTDuration = duration;
    }

    public void setExpandHeapDuration(double duration) {
        this.expandHeapDuration = duration;
    }

    public void setOtherPhaseDurations(double duration) {
        this.otherPhaseDurations = duration;
    }

    public double getParallelPhaseDurationTime() {
        return this.parallelPhaseDuration;
    }

    public int getGcWorkers() {
        return gcWorkers;
    }

    public int getEvacuationWorkersUsed() {
        return evacuationWorkersUsed;
    }

    public int getEvacuationWorkersAvailable() {
        return evacuationWorkersAvailable;
    }

    public double getCodeRootFixupDuration() {
        return codeRootFixupDuration;
    }

    public double getCodeRootMigrationDuration() {
        return codeRootMigrationDuration;
    }

    public double getCodeRootPurgeDuration() {
        return codeRootPurgeDuration;
    }

    public double getClearCTDuration() {
        return clearCTDuration;
    }

    public double getExpandHeapDuration() {
        return this.expandHeapDuration;
    }

    public double getStringDedupingDuration() {
        return this.stringDedupingDuration;
    }

    public int getStringDeduppingWorkers() {
        return this.stringDeduppingWorkers;
    }

    public StatisticalSummary getQueueFixupStatistics() {
        return this.queueFixupStatistics;
    }

    public StatisticalSummary getTableFixupStatistics() {
        return this.tableFixupStatistics;
    }

    public double getOtherPhaseDurations() {
        return otherPhaseDurations;
    }

    public void setWorkersStart(StatisticalSummary summary) {
        this.workersStart = summary;
    }

    public StatisticalSummary getWorkersStart() {
        return this.workersStart;
    }

    public void setWorkersEnd(StatisticalSummary summary) {
        this.workersEnd = summary;
    }

    public StatisticalSummary getWorkersEnd() {
        return this.workersEnd;
    }

    public void addProcessedBuffersSummary(StatisticalSummary summary) {
        this.processedBuffersSummary = summary;
    }

    public StatisticalSummary getProcessedBuffersSummary() {
        return this.processedBuffersSummary;
    }

    public void addWorkerActivity(String group, StatisticalSummary statisticalSummary) {
        if (group.endsWith("Other"))
            workerOther = statisticalSummary;
        if (group.endsWith("Total"))
            workerTotal = statisticalSummary;
    }

    public StatisticalSummary getWorkerOther() {
        return this.workerOther;
    }

    public StatisticalSummary getWorkerTotal() {
        return this.workerTotal;
    }

    public void addPhaseDuration(String key, double duration) {
        phaseDurations.put(key, duration);
    }

    public Iterator<String> phaseNames() {
        return phaseDurations.keySet().iterator();
    }

    public double phaseDurationFor(String phaseName) {
        return phaseDurations.get(phaseName);
    }

    public void addParallelPhaseSummary(String key, StatisticalSummary summary) {
        this.parallelPhaseSummaries.put(key, summary);
    }

    public Iterator<String> parallelPhaseNames() {
        return parallelPhaseSummaries.keySet().iterator();
    }

    public StatisticalSummary parallelPhaseSummaryFor(String phaseName) {
        return parallelPhaseSummaries.get(phaseName);
    }

    public void queueFixupStatistics(StatisticalSummary summary) {
        this.queueFixupStatistics = summary;
    }

    public void tableFixupStatistics(StatisticalSummary summary) {
        this.tableFixupStatistics = summary;
    }

    private final Map<String, Double> preEvacuateCSetPhase = new ConcurrentHashMap<>(3);
    private final Map<String, UnifiedStatisticalSummary> evacuateCSetPhase = new ConcurrentHashMap<>();
    private final Map<String, Double> postEvacuateCSetPhase = new ConcurrentHashMap<>();

    public void addPreEvacuationCollectionPhase(String name, double duration) {
        preEvacuateCSetPhase.put(name, duration);
    }

    public Stream<String> preEvacuateCSetPhaseNames() {
        return preEvacuateCSetPhase.keySet().stream();
    }

    public double preEvacuateCSetPhaseDuration(String name) {
        return preEvacuateCSetPhase.get(name);
    }

    public void addEvacuationCollectionPhase(String name, UnifiedStatisticalSummary summary) {
        evacuateCSetPhase.put(name, summary);
    }

    public Stream<String> evacuateCSetPhaseNames() {
        return evacuateCSetPhase.keySet().stream();
    }

    public StatisticalSummary evacuateCSetPhaseDuration(String name) {
        return evacuateCSetPhase.get(name);
    }

    public void addPostEvacuationCollectionPhase(String name, double summary) {
        postEvacuateCSetPhase.put(name, summary);
    }

    public Stream<String> postEvacuateCSetPhaseNames() {
        return postEvacuateCSetPhase.keySet().stream();
    }

    public double postEvacuateCSetPhaseDuration(String name) {
        return postEvacuateCSetPhase.get(name);
    }

}
