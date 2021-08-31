// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.ReferenceGCSummary;
import com.microsoft.gctoolkit.event.SurvivorMemoryPoolSummary;
import com.microsoft.gctoolkit.event.UnifiedCountSummary;
import com.microsoft.gctoolkit.event.UnifiedStatisticalSummary;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentCleanupForNextMark;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentClearClaimedMarks;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentCompleteCleanup;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentCreateLiveData;
import com.microsoft.gctoolkit.event.g1gc.ConcurrentScanRootRegion;
import com.microsoft.gctoolkit.event.g1gc.G1Cleanup;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentMark;
import com.microsoft.gctoolkit.event.g1gc.G1ConcurrentRebuildRememberedSets;
import com.microsoft.gctoolkit.event.g1gc.G1FullGC;
import com.microsoft.gctoolkit.event.g1gc.G1FullGCNES;
import com.microsoft.gctoolkit.event.g1gc.G1GCConcurrentEvent;
import com.microsoft.gctoolkit.event.g1gc.G1GCPauseEvent;
import com.microsoft.gctoolkit.event.g1gc.G1Mixed;
import com.microsoft.gctoolkit.event.g1gc.G1Remark;
import com.microsoft.gctoolkit.event.g1gc.G1SystemGC;
import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.g1gc.G1YoungInitialMark;
import com.microsoft.gctoolkit.parser.jvm.Decorators;
import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class G1GCForwardReference extends ForwardReference {

    private static final Logger LOGGER = Logger.getLogger(G1GCForwardReference.class.getName());

    private static int heapRegionSize = 0;
    private static long minHeapSize;
    private static long initialHeapSize;
    private static long maxHeapSize;

    public static void setHeapRegionSize(int sizeInMegaBytes) {
        heapRegionSize = sizeInMegaBytes;
    }

    public static int getHeapRegionSize() {
        return heapRegionSize;
    }

    private GarbageCollectionTypes pausePhaseDuringConcurrentCycle = null;
    private GarbageCollectionTypes gcType = null;

    public G1GCForwardReference(Decorators decorators, int gcID) {
        super(decorators, gcID);
    }

    public boolean isConcurrentCycle() {
        return gcType == GarbageCollectionTypes.Concurrent_Cycle;
    }

    //bag of stuff to maybe eliminate
    public static void setMinHeapSize(long minHeapSize) {
        G1GCForwardReference.minHeapSize = minHeapSize;
    }

    public static long getMinHeapSize() {
        return minHeapSize;
    }

    public static void setInitialHeapSize(long initialHeapSize) {
        G1GCForwardReference.initialHeapSize = initialHeapSize;
    }

    public static long getInitialHeapSize() {
        return initialHeapSize;
    }

    public static void setMaxHeapSize(long maxHeapSize) {
        G1GCForwardReference.maxHeapSize = maxHeapSize;
    }

    public void setGcType(GarbageCollectionTypes garbageCollectionType) {
        if ((this.gcType != null) && (gcType != garbageCollectionType)) {
            throw new IllegalArgumentException("attempting to redefine GC Type from" + this.gcType + " to " + garbageCollectionType);
        }
        gcType = garbageCollectionType;
    }

    private GarbageCollectionTypes concurrentPhase;

    public void setConcurrentPhase(GarbageCollectionTypes phase) {
        this.concurrentPhase = phase;
    }

    public GarbageCollectionTypes getConcurrentPhase() {
        return concurrentPhase;
    }


    /**
     * memory pool statistics
     */
    private final static int HEAP_OCCUPANCY_BEFORE_COLLECTION = 0;
    private final static int HEAP_OCCUPANCY_AFTER_COLLECTION = 1;
    private final static int HEAP_SIZE_BEFORE_COLLECTION = 2;
    private final static int HEAP_SIZE_AFTER_COLLECTION = 3;
    private final static int EDEN_OCCUPANCY_BEFORE_COLLECTION = 4;
    private final static int EDEN_OCCUPANCY_AFTER_COLLECTION = 5;
    private final static int EDEN_SIZE_BEFORE_COLLECTION = 6;
    private final static int EDEN_SIZE_AFTER_COLLECTION = 7;
    private final static int SURVIVOR_OCCUPANCY_BEFORE_COLLECTION = 8;
    private final static int SURVIVOR_OCCUPANCY_AFTER_COLLECTION = 9;
    private final static int SURVIVOR_SIZE_BEFORE_COLLECTION = 10;
    private final static int SURVIVOR_SIZE_AFTER_COLLECTION = 11;
    private final static int YOUNG_OCCUPANCY_BEFORE_COLLECTION = 12;
    private final static int YOUNG_OCCUPANCY_AFTER_COLLECTION = 13;
    private final static int YOUNG_SIZE_BEFORE_COLLECTION = 14;
    private final static int YOUNG__SIZE_AFTER_COLLECTION = 15;
    private final static int OLD_OCCUPANCY_BEFORE_COLLECTION = 16;
    private final static int OLD_OCCUPANCY_AFTER_COLLECTION = 17;
    private final static int OLD_SIZE_BEFORE_COLLECTION = 18;
    private final static int OLD_SIZE_AFTER_COLLECTION = 19;
    private final static int HUMONGOUS_OCCUPANCY_BEFORE_COLLECTION = 20;
    private final static int HUMONGOUS_OCCUPANCY_AFTER_COLLECTION = 21;
    private final static int HUMONGOUS_SIZE_BEFORE_COLLECTION = 22;
    private final static int HUMONGOUS_SIZE_AFTER_COLLECTION = 23;
    private final static int METASPACE_OCCUPANCY_BEFORE_COLLECTION = 24;
    private final static int METASPACE_OCCUPANCY_AFTER_COLLECTION = 25;
    private final static int METASPACE_SIZE_BEFORE_COLLECTION = 26;
    private final static int METASPACE_SIZE_AFTER_COLLECTION = 27;
    private final static int METASPACE_COMMITTED_BEFORE_COLLECTION = 28;
    private final static int METASPACE_COMMITTED_AFTER_COLLECTION = 29;
    private final static int METASPACE_RESERVED_BEFORE_COLLECTION = 30;
    private final static int METASPACE_RESERVED_AFTER_COLLECTION = 31;
    private final static int CLASSSPACE_OCCUPANCY_BEFORE_COLLECTION = 32;
    private final static int CLASSSPACE_OCCUPANCY_AFTER_COLLECTION = 33;
    private final static int CLASSSPACE_SIZE_BEFORE_COLLECTION = 34;
    private final static int CLASSSPACE_SIZE_AFTER_COLLECTION = 35;
    private final static int CLASSSPACE_COMMITTED_BEFORE_COLLECTION = 36;
    private final static int CLASSSPACE_COMMITTED_AFTER_COLLECTION = 37;
    private final static int CLASSSPACE_RESERVED_BEFORE_COLLECTION = 38;
    private final static int CLASSSPACE_RESERVED_AFTER_COLLECTION = 39;

    private final long[] memoryPoolMeasurment = {
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L};

    private boolean setMemoryPoolMeasurement(int index, long value) {
        if (memoryPoolMeasurment[index] < 0L) {
            memoryPoolMeasurment[index] = value;
            return true;
        }
        return false;
    }

    public boolean setHeapOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(HEAP_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setHeapOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(HEAP_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setHeapSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(HEAP_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setHeapSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(HEAP_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setEdenOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(EDEN_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setEdenOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(EDEN_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setEdenSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(EDEN_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setEdenSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(EDEN_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setSurvivorOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(SURVIVOR_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setSurvivorOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(SURVIVOR_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setSurvivorSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(SURVIVOR_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setSurvivorSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(SURVIVOR_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setYoungOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(YOUNG_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setYoungOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(YOUNG_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setYoungSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(YOUNG_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setYoungSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(YOUNG__SIZE_AFTER_COLLECTION, value);
    }

    public boolean setOldOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(OLD_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setOldOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(OLD_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setOldSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(OLD_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setOldSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(OLD_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setHumongousOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(HUMONGOUS_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setHumongousOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(HUMONGOUS_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setHumongousSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(HUMONGOUS_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setHumongousSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(HUMONGOUS_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setMetaspaceOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setMetaspaceOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setMetaspaceSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setMetaspaceSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setMetaspaceCommittedBeforeCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_COMMITTED_BEFORE_COLLECTION, value);
    }

    public boolean setMetaspaceCommittedAfterCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_COMMITTED_AFTER_COLLECTION, value);
    }

    public boolean setMetaspaceReservedBeforeCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_RESERVED_BEFORE_COLLECTION, value);
    }

    public boolean setMetaspaceReservedAfterCollection(long value) {
        return setMemoryPoolMeasurement(METASPACE_RESERVED_AFTER_COLLECTION, value);
    }

    public boolean setClassspaceOccupancyBeforeCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_OCCUPANCY_BEFORE_COLLECTION, value);
    }

    public boolean setClassspaceOccupancyAfterCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_OCCUPANCY_AFTER_COLLECTION, value);
    }

    public boolean setClassspaceSizeBeforeCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_SIZE_BEFORE_COLLECTION, value);
    }

    public boolean setClassspaceSizeAfterCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_SIZE_AFTER_COLLECTION, value);
    }

    public boolean setClassspaceCommittedBeforeCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_COMMITTED_BEFORE_COLLECTION, value);
    }

    public boolean setClassspaceCommittedAfterCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_COMMITTED_AFTER_COLLECTION, value);
    }

    public boolean setClassspaceReservedBeforeCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_RESERVED_BEFORE_COLLECTION, value);
    }

    public boolean setClassspaceReservedAfterCollection(long value) {
        return setMemoryPoolMeasurement(CLASSSPACE_RESERVED_AFTER_COLLECTION, value);
    }

    // ******
    // Reference processing
    final static int SOFT_REFERENCE = 0;
    final static int WEAK_REFERENCE = 1;
    final static int PHANTOM_REFERENCE = 2;
    final static int FINAL_REFERENCE = 3;
    final static int JNI_WEAK_REFERENCE = 4;
    final static int CLEANER_REFERENCE = 5;
    double[] referenceProcessingDuarations = {-1.0d, -1.0d, -1.0d, -1.0d, -1.0d, -1.0d};
    int[] referenceCounts = {-1, -1, -1, -1, -1, -1};

    public void setSoftReferenceProcessingDuation(double duration) {
        referenceProcessingDuarations[SOFT_REFERENCE] = duration;
    }

    public void setWeakReferenceProcessingDuration(double duration) {
        referenceProcessingDuarations[WEAK_REFERENCE] = duration;
    }

    public void setPhantomReferenceProcessingDuration(double duration) {
        referenceProcessingDuarations[PHANTOM_REFERENCE] = duration;
    }

    public void setFinalReferenceProcessingDuration(double duration) {
        referenceProcessingDuarations[FINAL_REFERENCE] = duration;
    }

    public void setJniWeakReferenceProcessingDuration(double duration) {
        referenceProcessingDuarations[JNI_WEAK_REFERENCE] = duration;
    }

    public void setCleanerReferenceProcessingDuration(double duration) {
        referenceProcessingDuarations[CLEANER_REFERENCE] = duration;
    }

    public void setReferenceCounts(int soft, int weak, int finalReferenceCount, int phantom) {
        referenceCounts[WEAK_REFERENCE] = weak;
        referenceCounts[SOFT_REFERENCE] = soft;
        referenceCounts[PHANTOM_REFERENCE] = phantom;
        referenceCounts[FINAL_REFERENCE] = finalReferenceCount;
    }

    private boolean hasReferenceGCSummary() {
        return referenceCounts[0] != -1;
    }

    private ReferenceGCSummary generateReferenceGCSummary() {
        ReferenceGCSummary summary = new ReferenceGCSummary();
        summary.addSoftReferences(getStartTime(), referenceCounts[SOFT_REFERENCE], referenceProcessingDuarations[SOFT_REFERENCE]);
        summary.addWeakReferences(getStartTime(), referenceCounts[WEAK_REFERENCE], referenceProcessingDuarations[WEAK_REFERENCE]);
        summary.addPhantomReferences(getStartTime(), referenceCounts[PHANTOM_REFERENCE], referenceProcessingDuarations[PHANTOM_REFERENCE]);
        summary.addFinalReferences(getStartTime(), referenceCounts[FINAL_REFERENCE], referenceProcessingDuarations[FINAL_REFERENCE]);
        summary.addJNIWeakReferences(getStartTime(), referenceCounts[JNI_WEAK_REFERENCE], referenceProcessingDuarations[JNI_WEAK_REFERENCE]);
        return summary;
    }


    // ****
    // Young phases
    private final static double NOT_SET = -1.0d;

    public final static int PRE_EVACUATE_COLLECTION_SET = 0;
    public final static int EVACUATE_COLLECTION_SET = 1;
    public final static int POST_EVACUATE_COLLECTION_SET = 2;
    public final static int OTHER = 3;

    private final double[] youngCollectionPhases = {NOT_SET, NOT_SET, NOT_SET, NOT_SET};
    private final Map<String, Double> preEvacuateCSetPhaseDurations = new ConcurrentHashMap<>(3);
    private final Map<String, UnifiedStatisticalSummary> evacuateCSetPhaseDurations = new ConcurrentHashMap<>();
    private final Map<String, Double> postEvacuateCSetPhaseDurations = new ConcurrentHashMap<>();

    public void setPreEvacuateCSetDuration(double duration) {
        this.youngCollectionPhases[PRE_EVACUATE_COLLECTION_SET] = duration;
    }

    public void recordPreEvacuateCSetPhaseDuration(String phase, double duration) {
        preEvacuateCSetPhaseDurations.put(phase, duration);
    }

    public Stream<String> preEvacuateCSetPhaseNames() {
        return preEvacuateCSetPhaseDurations.keySet().stream();
    }

    public double preEvacuateCSetPhaseDuration(String phaseName) {
        return preEvacuateCSetPhaseDurations.get(phaseName);
    }

    public void setEvacuationCSetDuration(double duration) {
        this.youngCollectionPhases[EVACUATE_COLLECTION_SET] = duration;
    }

    public void recordEvacuateCSetPhaseDuration(String phase, UnifiedStatisticalSummary stats) {
        evacuateCSetPhaseDurations.put(phase, stats);
    }

    public Stream<String> evacuateCSetPhaseNames() {
        return evacuateCSetPhaseDurations.keySet().stream();
    }

    public UnifiedStatisticalSummary evacuateCSetPhaseDuration(String phaseName) {
        return evacuateCSetPhaseDurations.get(phaseName);
    }

    public void setPostEvacuateCSetDuration(double duration) {
        this.youngCollectionPhases[POST_EVACUATE_COLLECTION_SET] = duration;
    }

    public void recordPostEvacuateCSetPhaseDuration(String phase, double duration) {
        postEvacuateCSetPhaseDurations.put(phase, duration);
    }

    public Stream<String> postEvacuateCSetPhaseNames() {
        return postEvacuateCSetPhaseDurations.keySet().stream();
    }

    public double postEvacuateCSetPhaseDuration(String phaseName) {
        return postEvacuateCSetPhaseDurations.get(phaseName);
    }

    public void setOtherDuration(double duration) {
        this.youngCollectionPhases[OTHER] = duration;
    }

    private UnifiedCountSummary unifiedCountSummary = null;

    public void setProcessedBuffersSummary(UnifiedCountSummary summary) {
        unifiedCountSummary = summary;
    }

    private UnifiedCountSummary terminationAttempts = null;

    public void setTerminationAttempts(UnifiedCountSummary summary) {
        this.terminationAttempts = summary;
    }

    //
    private boolean toSpaceExhausted = false;

    public void toSpaceExhausted() {
        toSpaceExhausted = true;
    }

    int evacuationWorkersUsed = 0;
    int evacuationWorkersAvailable = 0;

    public void evacuationWorkers(int workersUsed, int available) {
        this.evacuationWorkersUsed = workersUsed;
        this.evacuationWorkersAvailable = available;
    }

    // Concurrent Remark values
    int concurrentMarkWorkersUsed = 0;
    int concurrentMarkWorkersAvailable = 0;

    public void concurrentMarkWorkers(int used, int available) {
        this.concurrentMarkWorkersUsed = used;
        this.concurrentMarkWorkersAvailable = available;
    }

    private double markFromRootsDuration = -1.0d;
    private double precleanDuration = -1.0d;

    public void setMarkFromRootsDuration(double duration) {
        this.markFromRootsDuration = duration;
    }

    public void setPrecleanDuration(double duration) {
        this.precleanDuration = duration;
    }

    private boolean aborted = false;

    public void abortConcurrentMark() {
        aborted = true;
    }

    // builders

    private SurvivorMemoryPoolSummary getSurvivorMemoryPoolSummary() {
        if (memoryPoolMeasurment[SURVIVOR_OCCUPANCY_BEFORE_COLLECTION] == -1L)
            return null;
        return new SurvivorMemoryPoolSummary(memoryPoolMeasurment[SURVIVOR_OCCUPANCY_BEFORE_COLLECTION], memoryPoolMeasurment[SURVIVOR_OCCUPANCY_AFTER_COLLECTION], memoryPoolMeasurment[SURVIVOR_SIZE_AFTER_COLLECTION]);
    }

    private static final int OCCUPANCY_BEFORE_OFFSET = 0;
    private static final int OCCUPANCY_AFTER_OFFSET = 1;
    private static final int SIZE_BEFORE_OFFSET = 2;
    private static final int SIZE_AFTER_OFFSET = 3;

    private MemoryPoolSummary getMemoryPoolSummary(int offset) {
        if (memoryPoolMeasurment[offset + OCCUPANCY_BEFORE_OFFSET] == -1L) //do we have recorded values
            return null;
        //do we know the size of the memory pool prior to the collection
        long sizeBeforeCollection = (memoryPoolMeasurment[offset + SIZE_BEFORE_OFFSET] > -1L) ? memoryPoolMeasurment[offset + SIZE_BEFORE_OFFSET] : memoryPoolMeasurment[offset + SIZE_AFTER_OFFSET];
        return new MemoryPoolSummary(memoryPoolMeasurment[offset + OCCUPANCY_BEFORE_OFFSET], sizeBeforeCollection, memoryPoolMeasurment[offset + OCCUPANCY_AFTER_OFFSET], memoryPoolMeasurment[offset + SIZE_AFTER_OFFSET]);
    }

    private void fillInMemoryPoolStats(G1GCPauseEvent collection) {
        //Eden, survivor, heap, meta and class space
        MemoryPoolSummary heap = getMemoryPoolSummary(HEAP_OCCUPANCY_BEFORE_COLLECTION);
        MemoryPoolSummary young = getMemoryPoolSummary(YOUNG_OCCUPANCY_BEFORE_COLLECTION);
        MemoryPoolSummary eden = getMemoryPoolSummary(EDEN_OCCUPANCY_BEFORE_COLLECTION);
        SurvivorMemoryPoolSummary survivor = getSurvivorMemoryPoolSummary();
        MemoryPoolSummary tenured = getMemoryPoolSummary(OLD_OCCUPANCY_BEFORE_COLLECTION);
        MemoryPoolSummary humongous = getMemoryPoolSummary(HUMONGOUS_OCCUPANCY_BEFORE_COLLECTION);
        if (heap != null && eden != null && survivor != null) {
            collection.addMemorySummary(eden, survivor, heap);
        } else if (eden == null && survivor == null && heap != null) {
            collection.addMemorySummary(heap);
        } //else
        //need to consider other possible combinations.
    }

    /*
        private final static int METASPACE_OCCUPANCY_BEFORE_COLLECTION = 24;
    private final static int METASPACE_OCCUPANCY_AFTER_COLLECTION = 25;
    private final static int METASPACE_SIZE_BEFORE_COLLECTION = 26;
    private final static int METASPACE_SIZE_AFTER_COLLECTION = 27;
    private final static int METASPACE_COMMITTED_BEFORE_COLLECTION = 28;
    private final static int METASPACE_COMMITTED_AFTER_COLLECTION = 29;
    private final static int METASPACE_RESERVED_BEFORE_COLLECTION = 30;
    private final static int METASPACE_RESERVED_AFTER_COLLECTION = 31;
     */
    private void fillInMetaspaceStats(G1GCPauseEvent collection) {
        collection.addPermOrMetaSpaceRecord(getMemoryPoolSummary(METASPACE_OCCUPANCY_BEFORE_COLLECTION));
    }

    private void fullInInternalPhases(G1FullGC collection) {
        int index = 0;
        String key;
        while ((key = fullGCInternalPhaseOrder.get(++index)) != null) {
            collection.addInternalPhase(key, fullGCInternalPhases.get(key));
        }
    }

    private void fillInPhases(G1Young collection) {
        collection.addPhaseDuration("Pre Evacuate Collection", youngCollectionPhases[PRE_EVACUATE_COLLECTION_SET]);
        collection.addPhaseDuration("Evacuate Collection", youngCollectionPhases[EVACUATE_COLLECTION_SET]);
        collection.addPhaseDuration("Post Evacuate Collection Set", youngCollectionPhases[POST_EVACUATE_COLLECTION_SET]);
        collection.addPhaseDuration("Other", youngCollectionPhases[OTHER]);

        preEvacuateCSetPhaseNames().forEach(name -> collection.addPreEvacuationCollectionPhase(name, preEvacuateCSetPhaseDuration(name)));
        evacuateCSetPhaseNames().forEach(name -> collection.addEvacuationCollectionPhase(name, evacuateCSetPhaseDuration(name)));
        postEvacuateCSetPhaseNames().forEach(name -> collection.addPostEvacuationCollectionPhase(name, postEvacuateCSetPhaseDuration(name)));
    }

    private DateTimeStamp pausePhaseDuringConcurrentCycleTime = null;
    private double pausePhaseDuringConcurrentCycleDuration = -1.0d;

    public void pausePhaseDuringConcurrentCycle(GarbageCollectionTypes pausePhase) {
        this.pausePhaseDuringConcurrentCycle = pausePhase;
    }

    public void pausePhaseDuringConcurrentCycleStart(DateTimeStamp clock) {
        pausePhaseDuringConcurrentCycleTime = clock;
    }

    public void pausePhaseDuringConcurrentCycleDuration(double duration) {
        pausePhaseDuringConcurrentCycleDuration = duration;
    }

    private double finalizeMarkingDuration = -1.0d;
    private double systemDictionaryUnloadingDuration = -1.0d;

    public void finalizeMarkingDuration(double duration) {
        this.finalizeMarkingDuration = duration;
    }

    public void systemDictionaryUnloadingDuration(double duration) {
        this.systemDictionaryUnloadingDuration = duration;
    }

    private int stringTableProcessed = -1;
    private int stringTableRemoved = -1;

    public void stringTableProcessedAndRemoved(int processed, int removed) {
        this.stringTableProcessed = processed;
        this.stringTableRemoved = removed;
    }

    private int symbolTableProcessed = -1;
    private int symbolTableRemoved = -1;

    public void symbolTableProcessedAndRemoved(int processed, int removed) {
        this.symbolTableProcessed = processed;
        this.symbolTableRemoved = removed;
    }

    private double parallelUnloadingDuration = -1.0d;

    public void parallelUnloadingDuration(double duration) {
        this.parallelUnloadingDuration = duration;
    }

    public G1GCConcurrentEvent buildConcurrentEvent() {
        switch (getConcurrentPhase()) {
            case ConcurrentClearClaimedMarks:
                return new ConcurrentClearClaimedMarks(getStartTime(), getDuration());
            case ConcurrentScanRootRegions:
                return new ConcurrentScanRootRegion(getStartTime(), getDuration());
            case Concurrent_Mark:
                return buildConcurrentMark();
            case ConcurrentCompleteCleanup:
                return new ConcurrentCompleteCleanup(getStartTime(), getDuration());
            case ConcurrentCreateLiveData:
                return new ConcurrentCreateLiveData(getStartTime(), getDuration());
            case ConcurrentCleanupForNextMark:
                return new ConcurrentCleanupForNextMark(getStartTime(), getDuration());
            case G1ConcurrentRebuildRememberedSets:
                return new G1ConcurrentRebuildRememberedSets(getStartTime(), getDuration());
            default:
                LOGGER.warning("Unrecognized Concurrent Event " + getConcurrentPhase());
        }
        return null;
    }

    /**
     * gcType == null -> likely an incomplete record.
     * @return
     */
    G1GCPauseEvent buildEvent() {
        if ( this.gcType == null) {
            LOGGER.warning("GC Event is undefined (null)");
            return null;
        }
        switch (this.gcType) {
            case Young:
                return buildYoung();
            case Initial_Mark:
                return buildInitialMark();
            case Mixed:
                return buildMixed();
            case G1GCFull:
                return buildFull();
            case Concurrent_Cycle:
                switch (pausePhaseDuringConcurrentCycle) {
                    case G1GCRemark:
                        return buildRemark();
                    case G1GCCleanup:
                        return buildCleanup();
                    default:
                        LOGGER.warning("Unrecognized Concurrent Pause Event " + getConcurrentPhase());
                }
                return null;
            default:
                LOGGER.warning("Unrecognized Event " + gcType);
                return null;
        }
    }

    private G1Young buildYoung(G1Young collection) {
        fillInMemoryPoolStats(collection);
        fillInMetaspaceStats(collection);
        fillInPhases(collection);
        if (toSpaceExhausted) collection.toSpaceExhausted();
        if (hasReferenceGCSummary())
            collection.add(generateReferenceGCSummary());
        collection.addCPUSummary(getCPUSummary());
        return collection;
    }

    private G1Young buildYoung() {
        return buildYoung(new G1Young(getStartTime(), getGCCause(), getDuration()));
    }

    private G1YoungInitialMark buildInitialMark() {
        return (G1YoungInitialMark) buildYoung(new G1YoungInitialMark(getStartTime(), getGCCause(), getDuration()));
    }

    private G1Mixed buildMixed() {
        return (G1Mixed) buildYoung(new G1Mixed(getStartTime(), getGCCause(), getDuration()));
    }


    private G1ConcurrentMark buildConcurrentMark() {
        G1ConcurrentMark concurrentMark = new G1ConcurrentMark(getStartTime(), getDuration());
        if (aborted)
            concurrentMark.abort();
        if (markFromRootsDuration > -1.0d) {
            concurrentMark.setMarkFromRootsDuration(markFromRootsDuration);
            concurrentMark.setActiveWorkerThreads(concurrentMarkWorkersUsed);
            concurrentMark.setAvailableWorkerThreads(concurrentMarkWorkersAvailable);
        }

        if (precleanDuration > -1.0)
            concurrentMark.setPrecleanDuration(precleanDuration);

        return concurrentMark;
    }

    private G1Remark buildRemark() {
        G1Remark remark = new G1Remark(pausePhaseDuringConcurrentCycleTime, 0.0d, pausePhaseDuringConcurrentCycleDuration);
        if (hasReferenceGCSummary())
            remark.add(generateReferenceGCSummary());
        fillInMemoryPoolStats(remark);
        remark.addCPUSummary(getCPUSummary());
        return remark;
    }

    private G1Cleanup buildCleanup() {
        G1Cleanup cleanup = new G1Cleanup(pausePhaseDuringConcurrentCycleTime, pausePhaseDuringConcurrentCycleDuration);
        fillInMemoryPoolStats(cleanup);
        cleanup.addCPUSummary(getCPUSummary());
        return cleanup;
    }

    /*   todo: capture phases????
    [226.179s][info ][gc,phases,start] GC(1198) Phase 1: Mark live objects
    [226.310s][info ][gc,phases      ] GC(1198) Phase 1: Mark live objects 131.379ms
    [226.310s][info ][gc,phases,start] GC(1198) Phase 2: Compute new object addresses
    [226.354s][info ][gc,phases      ] GC(1198) Phase 2: Compute new object addresses 44.149ms
    [226.354s][info ][gc,phases,start] GC(1198) Phase 3: Adjust pointers
    [226.432s][info ][gc,phases      ] GC(1198) Phase 3: Adjust pointers 77.897ms
    [226.432s][info ][gc,phases,start] GC(1198) Phase 4: Move objects
    [226.461s][info ][gc,phases      ] GC(1198) Phase 4: Move objects 28.974ms
 */
    private G1FullGC buildFull() {
        G1FullGC collection;
        if (getGCCause() == GCCause.JAVA_LANG_SYSTEM)
            collection = new G1SystemGC(getStartTime(), getDuration());
        else
            collection = new G1FullGCNES(getStartTime(), getGCCause(), getDuration());

        // [226.310s][info ][gc,stringtable ] GC(1198) Cleaned string and symbol table, strings: 7394 processed, 0 removed, symbols: 55496 processed, 0 removed
        // [226.310s][debug][gc,phases      ] GC(1198) Scrub String and Symbol Tables 0.451ms.
        //todo: collection.addStringTableSymbolTable();
        //collection.addPermOrMetaSpaceRecord(metaSpaceSummary);
        //[226.310s][debug][gc,phases      ] GC(1198) Class Unloading 3.481ms
        //collection.classUnloading()  todo: fill in
        fillInMemoryPoolStats(collection);
        fullInInternalPhases(collection);
        if (hasReferenceGCSummary())
            collection.add(generateReferenceGCSummary());
        collection.addCPUSummary(getCPUSummary());
        return collection;
    }

    private final Map<String, Double> fullGCInternalPhases = new ConcurrentHashMap<>();
    private final Map<Integer, String> fullGCInternalPhaseOrder = new ConcurrentHashMap<>();

    public void fullPhase(int integerGroup, String fullGCInternalPhase, double duration) {
        fullGCInternalPhaseOrder.put(integerGroup, fullGCInternalPhase);
        fullGCInternalPhases.put(fullGCInternalPhase, duration);
    }

    public String toString() {
        return gcType + ":" + getGCCause() + ":" + getDuration();
    }
}
