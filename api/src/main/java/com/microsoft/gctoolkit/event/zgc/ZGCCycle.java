package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.List;

public class ZGCCycle {
    private DateTimeStamp markRootsStart;
    private double markRootsDuration;
    private DateTimeStamp markFollowStart;
    private double markFollowDuration;
    private DateTimeStamp remapRootColoredStart;
    private double remapRootsColoredDuration;
    private DateTimeStamp remapRootsUncoloredStart;
    private double remapRootsUncoloredDuration;
    private DateTimeStamp remapRememberedStart;
    private double remapRememberedDuration;
    private ZGCMarkSummary markSummary;
    private ZGCPromotedSummary promotedSummary;
    private ZGCCompactedSummary compactedSummary;
    private ZGCCollectionType type;
    private ZGCPhase phase;
    private OccupancySummary usedOccupancySummary;
    private long gcId;
    private DateTimeStamp pauseMarkStartTimeStamp;
    private double pauseMarkStartDuration;
    private DateTimeStamp concurrentMarkTimeStamp;
    private double concurrentMarkDuration;
    private DateTimeStamp concurrentMarkFreeTimeStamp;
    private double concurrentMarkFreeDuration;
    private DateTimeStamp pauseMarkEndTimeStamp;
    private double pauseMarkEndDuration;
    private DateTimeStamp concurrentProcessNonStrongReferencesTimeStamp;
    private double concurrentProcessNonStrongReferencesDuration;
    private DateTimeStamp concurrentResetRelocationSetTimeStamp;
    private double concurrentResetRelocationSetDuration;
    private DateTimeStamp concurrentSelectRelocationSetTimeStamp;
    private double concurrentSelectRelocationSetDuration;
    private DateTimeStamp pauseRelocateStartTimeStamp;
    private double pauseMarkRelocateDuration;
    private DateTimeStamp concurrentRelocateTimeStamp;
    private double concurrentRelocateDuration;
    private DateTimeStamp concurrentMarkContinueTimeStamp;
    private double concurrentMarkContinueDuration;
    private DateTimeStamp concurrentRemapRootsStart;
    private double concurrentRemapRootsDuration;
    private double[] load = new double[3];
    private double[] mmu = new double[6];

    //Memory
    private ZGCMemoryPoolSummary markStart;
    private ZGCMemoryPoolSummary markEnd;
    private ZGCMemoryPoolSummary relocateStart;
    private ZGCMemoryPoolSummary relocateEnd;

    private ZGCLiveSummary liveSummary;
    private ZGCAllocatedSummary allocatedSummary;
    private ZGCGarbageSummary garbageSummary;
    private ZGCReclaimSummary reclaimSummary;
    private ZGCMemorySummary memorySummary;
    private ZGCMetaspaceSummary metaspaceSummary;
    private ZGCHeapCapacitySummary heapCapacitySummary;
    private ZGCNMethodSummary nMethodSummary;

    private ZGCPageSummary smallPageSummary;
    private ZGCPageSummary mediumPageSummary;
    private ZGCPageSummary largePageSummary;
    private long forwardingUsage;
    private List<ZGCPageAgeSummary> ageTableSummary;

    public ZGCReferenceSummary getSoftRefSummary() {
        return softRefSummary;
    }

    public ZGCReferenceSummary getWeakRefSummary() {
        return weakRefSummary;
    }

    public ZGCReferenceSummary getFinalRefSummary() {
        return finalRefSummary;
    }

    public ZGCReferenceSummary getPhantomRefSummary() {
        return phantomRefSummary;
    }

    private ZGCReferenceSummary softRefSummary;
    private ZGCReferenceSummary weakRefSummary;
    private ZGCReferenceSummary finalRefSummary;
    private ZGCReferenceSummary phantomRefSummary;

    public ZGCMarkSummary getMarkSummary() {
        return markSummary;
    }

    public void setMarkSummary(ZGCMarkSummary markSummary) {
        this.markSummary = markSummary;
    }

    public DateTimeStamp getPauseMarkStartTimeStamp() {
        return pauseMarkStartTimeStamp;
    }

    public double getPauseMarkStartDuration() {
        return pauseMarkStartDuration;
    }

    public void setPauseMarkStart(DateTimeStamp pauseMarkStartTimeStamp, double duration) {
        this.pauseMarkStartTimeStamp = pauseMarkStartTimeStamp;
        this.pauseMarkStartDuration = duration;
    }

    public long getGcId() {
        return gcId;
    }

    public void setGcId(long gcId) {
        this.gcId = gcId;
    }

    public DateTimeStamp getConcurrentMarkTimeStamp() {
        return concurrentMarkTimeStamp;
    }

    public double getConcurrentMarkDuration() {
        return concurrentMarkDuration;
    }

    public void setConcurrentMark(DateTimeStamp concurrentMarkTimeStamp, double duration) {
        this.concurrentMarkTimeStamp = concurrentMarkTimeStamp;
        this.concurrentMarkDuration = duration;
    }

    public DateTimeStamp getConcurrentMarkFreeTimeStamp() {
        return concurrentMarkFreeTimeStamp;
    }

    public double getConcurrentMarkFreeDuration() {
        return concurrentMarkFreeDuration;
    }

    public void setConcurrentMarkFree(DateTimeStamp concurrentMarkFreeStart, double duration) {
        this.concurrentMarkFreeTimeStamp = concurrentMarkFreeStart;
        this.concurrentMarkFreeDuration = duration;
    }

    public DateTimeStamp getPauseMarkEndTimeStamp() {
        return pauseMarkEndTimeStamp;
    }

    public double getPauseMarkEndDuration() {
        return pauseMarkEndDuration;
    }

    public void setPauseMarkEnd(DateTimeStamp pauseMarkEndTimeStamp, double duration) {
        this.pauseMarkEndTimeStamp = pauseMarkEndTimeStamp;
        this.pauseMarkEndDuration = duration;
    }

    public DateTimeStamp getConcurrentProcessNonStrongReferencesTimeStamp() {
        return concurrentProcessNonStrongReferencesTimeStamp;
    }

    public double getConcurrentProcessNonStrongReferencesDuration() {
        return concurrentProcessNonStrongReferencesDuration;
    }

    public void setConcurrentProcessNonStrongReferences(DateTimeStamp concurrentProcessNonStrongReferencesTimeStamp, double duration) {
        this.concurrentProcessNonStrongReferencesTimeStamp = concurrentProcessNonStrongReferencesTimeStamp;
        this.concurrentProcessNonStrongReferencesDuration = duration;
    }

    public DateTimeStamp getConcurrentResetRelocationSetTimeStamp() {
        return concurrentResetRelocationSetTimeStamp;
    }

    public double getConcurrentResetRelocationSetDuration() {
        return concurrentResetRelocationSetDuration;
    }

    public void setConcurrentResetRelocationSet(DateTimeStamp concurrentResetRelocationSetTimeStamp, double duration) {
        this.concurrentResetRelocationSetTimeStamp = concurrentResetRelocationSetTimeStamp;
        this.concurrentResetRelocationSetDuration = duration;
    }

    public DateTimeStamp getConcurrentSelectRelocationSetTimeStamp() {
        return concurrentSelectRelocationSetTimeStamp;
    }

    public double getConcurrentSelectRelocationSetDuration() {
        return concurrentSelectRelocationSetDuration;
    }

    public void setConcurrentSelectRelocationSet(DateTimeStamp concurrentSelectRelocationSetTimeStamp, double duration) {
        this.concurrentSelectRelocationSetTimeStamp = concurrentSelectRelocationSetTimeStamp;
        this.concurrentSelectRelocationSetDuration = duration;
    }

    public DateTimeStamp getPauseRelocateStartTimeStamp() {
        return pauseRelocateStartTimeStamp;
    }

    public double getPauseRelocateStartDuration() {
        return pauseMarkRelocateDuration;
    }

    public void setPauseRelocateStart(DateTimeStamp pauseRelocateStartTimeStamp, double duration) {
        this.pauseRelocateStartTimeStamp = pauseRelocateStartTimeStamp;
        this.pauseMarkRelocateDuration = duration;
    }

    public DateTimeStamp getConcurrentRelocateTimeStamp() {
        return concurrentRelocateTimeStamp;
    }

    public double getConcurrentRelocateDuration() {
        return concurrentRelocateDuration;
    }

    public void setConcurrentRelocate(DateTimeStamp concurrentRelocateTimeStamp, double duration) {
        this.concurrentRelocateTimeStamp = concurrentRelocateTimeStamp;
        this.concurrentRelocateDuration = duration;
    }

    public void setConcurrentMarkContinue(DateTimeStamp concurrentMarkContinueTimeStamp, double duration) {
        this.concurrentMarkContinueTimeStamp = concurrentMarkContinueTimeStamp;
        this.concurrentMarkContinueDuration = duration;
    }

    public ZGCMemoryPoolSummary getMarkStart() {
        return markStart;
    }

    public void setMarkStart(ZGCMemoryPoolSummary summary) {
        this.markStart = summary;
    }

    public ZGCMemoryPoolSummary getMarkEnd() {
        return markEnd;
    }

    public void setMarkEnd(ZGCMemoryPoolSummary summary) {
        this.markEnd = summary;
    }

    public ZGCMemoryPoolSummary getRelocateStart() {
        return relocateStart;
    }

    public void setRelocateStart(ZGCMemoryPoolSummary summary) {
        this.relocateStart = summary;
    }

    public ZGCMemoryPoolSummary getRelocateEnd() {
        return relocateEnd;
    }

    public void setRelocateEnd(ZGCMemoryPoolSummary summary) {
        this.relocateEnd = summary;
    }

    public ZGCLiveSummary getLiveSummary() {
        return liveSummary;
    }

    public void setLiveSummary(ZGCLiveSummary summary) {
        this.liveSummary = summary;
    }

    public ZGCAllocatedSummary getAllocatedSummary() {
        return allocatedSummary;
    }

    public void setAllocatedSummary(ZGCAllocatedSummary summary) {
        this.allocatedSummary = summary;
    }

    public ZGCGarbageSummary getGarbageSummary() {
        return garbageSummary;
    }

    public void setGarbageSummary(ZGCGarbageSummary summary) {
        this.garbageSummary = summary;
    }

    public ZGCReclaimSummary getReclaimSummary() {
        return reclaimSummary;
    }

    public void setReclaimSummary(ZGCReclaimSummary summary) {
        this.reclaimSummary = summary;
    }

    public ZGCMemorySummary getMemorySummary() {
        return memorySummary;
    }

    public void setMemorySummary(ZGCMemorySummary summary) {
        this.memorySummary = summary;
    }

    public ZGCMetaspaceSummary getMetaspaceSummary() {
        return metaspaceSummary;
    }

    public void setMetaspaceSummary(ZGCMetaspaceSummary summary) {
        this.metaspaceSummary = summary;
    }

    public void setLoadAverages(double[] load) {
        this.load = load;
    }

    public double getLoadAverageAt(int time) {
        switch (time) {
            case 1:
                return load[0];
            case 5:
                return load[1];
            case 15:
                return load[2];
            default:
                return 0.0d;
        }
    }

    public void setMMU(double[] mmu) {
        this.mmu = mmu;
    }

    public double getMMU(int percentage) {
        switch (percentage) {
            case 2:
                return mmu[0];
            case 5:
                return mmu[1];
            case 10:
                return mmu[2];
            case 20:
                return mmu[3];
            case 50:
                return mmu[4];
            case 100:
                return mmu[5];
            default:
                return 0.0d;
        }
    }

    public void setConcurrentRemapRoots(DateTimeStamp remapRootsStart, double remapRootsDuration) {
        this.concurrentRemapRootsStart = remapRootsStart;
        this.concurrentRemapRootsDuration = remapRootsDuration;
    }

    public void setMarkRoots(DateTimeStamp markRootsStart, double markRootsDuration) {
        this.markRootsStart = markRootsStart;
        this.markRootsDuration = markRootsDuration;
    }

    public void setMarkFollow(DateTimeStamp markFollowStart, double markFollowDuration) {
        this.markFollowStart = markFollowStart;
        this.markFollowDuration = markFollowDuration;
    }

    public void setRemapRootsColored(DateTimeStamp remapRootColoredStart, double remapRootsColoredDuration) {
        this.remapRootColoredStart = remapRootColoredStart;
        this.remapRootsColoredDuration = remapRootsColoredDuration;
    }

    public void setRemapRootsUncolored(DateTimeStamp remapRootsUncoloredStart, double remapRootsUncoloredDuration) {
        this.remapRootsUncoloredStart = remapRootsUncoloredStart;
        this.remapRootsUncoloredDuration = remapRootsUncoloredDuration;
    }

    public void setRemapRemembered(DateTimeStamp remapRememberedStart, double remapRememberedDuration) {

        this.remapRememberedStart = remapRememberedStart;
        this.remapRememberedDuration = remapRememberedDuration;
    }

    public ZGCPromotedSummary getPromotedSummary() {
        return promotedSummary;
    }

    public void setPromotedSummary(ZGCPromotedSummary promotedSummary) {

        this.promotedSummary = promotedSummary;
    }

    public ZGCCompactedSummary getCompactedSummary() {
        return compactedSummary;
    }

    public void setCompactedSummary(ZGCCompactedSummary compactedSummary) {
        this.compactedSummary = compactedSummary;
    }

    public void setusedOccupancySummary(OccupancySummary usedOccupancySummary) {

        this.usedOccupancySummary = usedOccupancySummary;
    }

    public OccupancySummary getUsedOccupancySummary() {
        return usedOccupancySummary;
    }

    public ZGCCollectionType getType() {
        return type;
    }

    public void setType(ZGCCollectionType type) {
        this.type = type;
    }

    public DateTimeStamp getMarkRootsStart() {
        return markRootsStart;
    }

    public double getMarkRootsDuration() {
        return markRootsDuration;
    }

    public DateTimeStamp getMarkFollowStart() {
        return markFollowStart;
    }

    public double getMarkFollowDuration() {
        return markFollowDuration;
    }

    public DateTimeStamp getRemapRootColoredStart() {
        return remapRootColoredStart;
    }

    public double getRemapRootsColoredDuration() {
        return remapRootsColoredDuration;
    }

    public DateTimeStamp getRemapRootsUncoloredStart() {
        return remapRootsUncoloredStart;
    }

    public double getRemapRootsUncoloredDuration() {
        return remapRootsUncoloredDuration;
    }

    public DateTimeStamp getRemapRememberedStart() {
        return remapRememberedStart;
    }

    public double getRemapRememberedDuration() {
        return remapRememberedDuration;
    }

    public double getPauseMarkRelocateDuration() {
        return pauseMarkRelocateDuration;
    }

    public DateTimeStamp getConcurrentMarkContinueTimeStamp() {
        return concurrentMarkContinueTimeStamp;
    }

    public double getConcurrentMarkContinueDuration() {
        return concurrentMarkContinueDuration;
    }

    public DateTimeStamp getConcurrentRemapRootsStart() {
        return concurrentRemapRootsStart;
    }

    public double getConcurrentRemapRootsDuration() {
        return concurrentRemapRootsDuration;
    }

    public double[] getLoad() {
        return load;
    }

    public double[] getMmu() {
        return mmu;
    }

    public void setPhase(ZGCPhase phase) {
        this.phase = phase;
    }

    public ZGCPhase getPhase() {
        return phase;
    }

    public void setSoftRefSummary(ZGCReferenceSummary softRefSummary) {
        this.softRefSummary = softRefSummary;
    }

    public void setWeakRefSummary(ZGCReferenceSummary weakRefSummary) {
        this.weakRefSummary = weakRefSummary;
    }

    public void setFinalRefSummary(ZGCReferenceSummary finalRefSummary) {
        this.finalRefSummary = finalRefSummary;
    }

    public void setPhantomRefSummary(ZGCReferenceSummary phantomRefSummary) {
        this.phantomRefSummary = phantomRefSummary;
    }

    public void setHeapCapacitySummary(ZGCHeapCapacitySummary heapCapacitySummary) {
        this.heapCapacitySummary = heapCapacitySummary;
    }

    public ZGCHeapCapacitySummary getHeapCapacitySummary() {
        return heapCapacitySummary;
    }

    public void setNMethodSummary(ZGCNMethodSummary nMethodSummary) {
        this.nMethodSummary = nMethodSummary;
    }

    public ZGCNMethodSummary getNMethodSummary() {
        return nMethodSummary;
    }

    public void setSmallPageSummary(ZGCPageSummary smallPageSummary) {
        this.smallPageSummary = smallPageSummary;
    }

    public void setMediumPageSummary(ZGCPageSummary mediumPageSummary) {
        this.mediumPageSummary = mediumPageSummary;
    }

    public void setLargePageSummary(ZGCPageSummary largePageSummary) {
        this.largePageSummary = largePageSummary;
    }

    public ZGCPageSummary getSmallPageSummary() {
        return smallPageSummary;
    }

    public ZGCPageSummary getMediumPageSummary() {
        return mediumPageSummary;
    }

    public ZGCPageSummary getLargePageSummary() {
        return largePageSummary;
    }

    public void setForwardingUsage(long forwardingUsage) {
        this.forwardingUsage = forwardingUsage;
    }

    public long getForwardingUsage() {
        return forwardingUsage;
    }

    public void setAgeTableSummary(List<ZGCPageAgeSummary> ageTableSummary) {
        this.ageTableSummary = ageTableSummary;
    }

    public List<ZGCPageAgeSummary> getAgeTableSummary() {
        return ageTableSummary;
    }
}
