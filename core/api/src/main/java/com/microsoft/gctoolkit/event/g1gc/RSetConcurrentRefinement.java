// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;


/*
Recent concurrent refinement statistics
        Processed 4907 cards
        Of 32 completed buffers:
        32 ( 93.8%) by concurrent RS threads.
        2 (  6.2%) by mutator threads.
        Did 0 coarsenings.
        Concurrent RS threads times (s)
        0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00
        Concurrent sampling threads times (s)
        0.00

        Current rem set statistics
        Total per region rem sets sizes = 230532K. Max = 1880K.
        105K (  0.0%) by 2 Young regions
        39K (  0.0%) by 4 Humonguous regions
        15506K (  6.7%) by 1569 Free regions
        214881K ( 93.2%) by 985 Old regions
        Static structures = 600K, free_lists = 10371K.
        9617950 occupied cards represented.
        1515 (  0.0%) entries by 2 Young regions
        4 (  0.0%) entries by 4 Humonguous regions
        0 (  0.0%) entries by 1569 Free regions
        9616431 (100.0%) entries by 985 Old regions
        Region with largest rem set = 0:(O)[0x00000002c0000000,0x00000002c0800000,0x00000002c0800000], size = 1880K, occupied = 1410K.
        Total heap region code root sets sizes = 171K.  Max = 97K.
        0K (  0.0%) by 2 Young regions
        0K (  0.0%) by 4 Humonguous regions
        24K ( 14.3%) by 1569 Free regions
        147K ( 85.7%) by 985 Old regions
        5416 code roots represented.
        0 (  0.0%) elements by 2 Young regions
        0 (  0.0%) elements by 4 Humonguous regions
        0 (  0.0%) elements by 1569 Free regions
        5416 (100.0%) elements by 985 Old regions
        Region with largest amount of code roots = 0:(O)[0x00000002c0000000,0x00000002c0800000,0x00000002c0800000], size = 97K, num_elems = 4.
*/
public class RSetConcurrentRefinement {

    private ConcurrentRefinementStatistics concurrentRefinementStatistics;
    private int totalPerRegionRSetSize;
    private int maxPerRegionRSetSize;

    private RegionsSummary regionsRSetSizeAndCardCount;
    private int youngRegionRSetSize;
    private int youngRegionsRSetCardCount;
    private int humonguousRegionRSetSize;
    private int humonguousRegionsRSetCardCount;
    private int staticStructuresSize;
    private int freeListSize;
    private int occupiedCards;

    private int entriesByYoungRegions;
    private int youngRegionCount;
    private int entriesByHumonguousRegions;
    private int humonguousRegionCount;
    private int entriesByFreeRegions;
    private int freeRegionsCount;
    private int entriesByOldRegions;
    private int oldRegionCount;
    private int largestRSetSize;
    private int largestRSetOccupancy;
    private int heapRegionCodeRootSetsSize;
    private int heapRegionCodeRootSetsMaxSize;
    private RegionsSummary regionCodeRootSizeAndRegions;
    private int codeRootsRepresented;
    private RegionsSummary elementsByRegionCounts;
    private int largestAmountOfCodeRootsSize;
    private int largestAmountOfCodeRootsNumberOfElements;

    public RSetConcurrentRefinement() {
    }

    public ConcurrentRefinementStatistics getConcurrentRefinementStatistics() {
        return this.concurrentRefinementStatistics;
    }

    public void setConcurrentRefinementStatistics(ConcurrentRefinementStatistics statistics) {
        this.concurrentRefinementStatistics = statistics;
    }

    public int getTotalPerRegionRSetSize() {
        return totalPerRegionRSetSize;
    }

    public void setTotalPerRegionRSetSize(int totalPerRegionRSetSize) {
        this.totalPerRegionRSetSize = totalPerRegionRSetSize;
    }

    public int getMaxPerRegionRSetSize() {
        return maxPerRegionRSetSize;
    }

    public void setMaxPerRegionRSetSize(int maxPerRegionRSetSize) {
        this.maxPerRegionRSetSize = maxPerRegionRSetSize;
    }

    public RegionsSummary getRegionsRSetSizeAndCardCount() {
        return regionsRSetSizeAndCardCount;
    }

    public void setRegionsRSetSizeAndCardCount(RegionsSummary regionsRSetSizeAndCardCount) {
        this.regionsRSetSizeAndCardCount = regionsRSetSizeAndCardCount;
    }

    public int getYoungRegionRSetSize() {
        return youngRegionRSetSize;
    }

    public void setYoungRegionRSetSize(int youngRegionRSetSize) {
        this.youngRegionRSetSize = youngRegionRSetSize;
    }

    public int getYoungRegionsRSetCardCount() {
        return youngRegionsRSetCardCount;
    }

    public void setYoungRegionsRSetCardCount(int youngRegionsRSetCardCount) {
        this.youngRegionsRSetCardCount = youngRegionsRSetCardCount;
    }

    public int getHumonguousRegionRSetSize() {
        return humonguousRegionRSetSize;
    }

    public void setHumonguousRegionRSetSize(int humonguousRegionRSetSize) {
        this.humonguousRegionRSetSize = humonguousRegionRSetSize;
    }

    public int getHumonguousRegionsRSetCardCount() {
        return humonguousRegionsRSetCardCount;
    }

    public void setHumonguousRegionsRSetCardCount(int humonguousRegionsRSetCardCount) {
        this.humonguousRegionsRSetCardCount = humonguousRegionsRSetCardCount;
    }

    public int getStaticStructuresSize() {
        return staticStructuresSize;
    }

    public void setStaticStructuresSize(int staticStructuresSize) {
        this.staticStructuresSize = staticStructuresSize;
    }

    public int getFreeListSize() {
        return freeListSize;
    }

    public void setFreeListSize(int freeListSize) {
        this.freeListSize = freeListSize;
    }

    public int getOccupiedCards() {
        return occupiedCards;
    }

    public void setOccupiedCards(int occupiedCards) {
        this.occupiedCards = occupiedCards;
    }

    public int getEntriesByYoungRegions() {
        return entriesByYoungRegions;
    }

    public void setEntriesByYoungRegions(int entriesByYoungRegions) {
        this.entriesByYoungRegions = entriesByYoungRegions;
    }

    public int getYoungRegionCount() {
        return youngRegionCount;
    }

    public void setYoungRegionCount(int youngRegionCount) {
        this.youngRegionCount = youngRegionCount;
    }

    public int getEntriesByHumonguousRegions() {
        return entriesByHumonguousRegions;
    }

    public void setEntriesByHumonguousRegions(int entriesByHumonguousRegions) {
        this.entriesByHumonguousRegions = entriesByHumonguousRegions;
    }

    public int getHumonguousRegionCount() {
        return humonguousRegionCount;
    }

    public void setHumonguousRegionCount(int humonguousRegionCount) {
        this.humonguousRegionCount = humonguousRegionCount;
    }

    public int getEntriesByFreeRegions() {
        return entriesByFreeRegions;
    }

    public void setEntriesByFreeRegions(int entriesByFreeRegions) {
        this.entriesByFreeRegions = entriesByFreeRegions;
    }

    public int getFreeRegionsCount() {
        return freeRegionsCount;
    }

    public void setFreeRegionsCount(int freeRegionsCount) {
        this.freeRegionsCount = freeRegionsCount;
    }

    public int getEntriesByOldRegions() {
        return entriesByOldRegions;
    }

    public void setEntriesByOldRegions(int entriesByOldRegions) {
        this.entriesByOldRegions = entriesByOldRegions;
    }

    public int getOldRegionCount() {
        return oldRegionCount;
    }

    public void setOldRegionCount(int oldRegionCount) {
        this.oldRegionCount = oldRegionCount;
    }

    public int getLargestRSetSize() {
        return largestRSetSize;
    }

    public void setLargestRSetSize(int largestRSetSize) {
        this.largestRSetSize = largestRSetSize;
    }

    public int getLargestRSetOccupancy() {
        return largestRSetOccupancy;
    }

    public void setLargestRSetOccupancy(int largestRSetOccupancy) {
        this.largestRSetOccupancy = largestRSetOccupancy;
    }

    public int getHeapRegionCodeRootSetsSize() {
        return heapRegionCodeRootSetsSize;
    }

    public void setHeapRegionCodeRootSetsSize(int heapRegionCodeRootSetsSize) {
        this.heapRegionCodeRootSetsSize = heapRegionCodeRootSetsSize;
    }

    public int getHeapRegionCodeRootSetsMaxSize() {
        return heapRegionCodeRootSetsMaxSize;
    }

    public void setHeapRegionCodeRootSetsMaxSize(int heapRegionCodeRootSetsMaxSize) {
        this.heapRegionCodeRootSetsMaxSize = heapRegionCodeRootSetsMaxSize;
    }

    public RegionsSummary getRegionCodeRootSizeAndRegions() {
        return regionCodeRootSizeAndRegions;
    }

    public void setRegionCodeRootSizeAndRegions(RegionsSummary regionCodeRootSizeAndRegions) {
        this.regionCodeRootSizeAndRegions = regionCodeRootSizeAndRegions;
    }

    public int getCodeRootsRepresented() {
        return codeRootsRepresented;
    }

    public void setCodeRootsRepresented(int codeRootsRepresented) {
        this.codeRootsRepresented = codeRootsRepresented;
    }

    public RegionsSummary getElementsByRegionCounts() {
        return elementsByRegionCounts;
    }

    public void setElementsByRegionCounts(RegionsSummary elementsByRegionCounts) {
        this.elementsByRegionCounts = elementsByRegionCounts;
    }

    public int getLargestAmountOfCodeRootsSize() {
        return largestAmountOfCodeRootsSize;
    }

    public void setLargestAmountOfCodeRootsSize(int largestAmountOfCodeRootsSize) {
        this.largestAmountOfCodeRootsSize = largestAmountOfCodeRootsSize;
    }

    public int getLargestAmountOfCodeRootsNumberOfElements() {
        return largestAmountOfCodeRootsNumberOfElements;
    }

    public void setLargestAmountOfCodeRootsNumberOfElements(int largestAmountOfCodeRootsNumberOfElements) {
        this.largestAmountOfCodeRootsNumberOfElements = largestAmountOfCodeRootsNumberOfElements;
    }

    /*
    Recent concurrent refinement statistics
        Processed 4907 cards
        Of 32 completed buffers:
        32 ( 93.8%) by concurrent RS threads.
        2 (  6.2%) by mutator threads.
        Did 0 coarsenings.
        Concurrent RS threads times (s)
        0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00
        Concurrent sampling threads times (s)
        0.00
     */

    class ConcurrentRefinementStatistics {
        private int cardsProcessed;
        private int completedBuffers;
        private int buffersCompletedByRSThreads;
        private int buffersCompletedByMutatorThreads;
        private int coarsenings;

        ConcurrentRefinementStatistics() {
        }

        public int getCardsProcessed() {
            return cardsProcessed;
        }

        public void setCardsProcessed(int cardsProcessed) {
            this.cardsProcessed = cardsProcessed;
        }

        public int getCompletedBuffers() {
            return completedBuffers;
        }

        public void setCompletedBuffers(int completedBuffers) {
            this.completedBuffers = completedBuffers;
        }

        public int getBuffersCompletedByRSThreads() {
            return buffersCompletedByRSThreads;
        }

        public void setBuffersCompletedByRSThreads(int buffersCompletedByRSThreads) {
            this.buffersCompletedByRSThreads = buffersCompletedByRSThreads;
        }

        public int getBuffersCompletedByMutatorThreads() {
            return buffersCompletedByMutatorThreads;
        }

        public void setBuffersCompletedByMutatorThreads(int buffersCompletedByMutatorThreads) {
            this.buffersCompletedByMutatorThreads = buffersCompletedByMutatorThreads;
        }

        public int getCoarsenings() {
            return coarsenings;
        }

        public void setCoarsenings(int coarsenings) {
            this.coarsenings = coarsenings;
        }
    }

    class RegionsSummary {

        private int youngRegionsStatistics;
        private int youngRegionsCount;
        private int humonguousRegionsStatistics;
        private int humonguousRegionsCount;
        private int freeRegionsStatistics;
        private int freeRegionsCount;
        private int oldRegionsStatistics;
        private int oldRegionsCount;

        RegionsSummary() {
        }

        void setYoungRegionsStatistics(int value) {
            youngRegionsStatistics = value;
        }

        void setYoungRegionsCount(int value) {
            youngRegionsCount = value;
        }

        void setHumonguousRegionsStatistics(int value) {
            humonguousRegionsStatistics = value;
        }

        void setHumonguousRegionsCount(int value) {
            humonguousRegionsCount = value;
        }

        void setFreeRegionsStatistics(int value) {
            freeRegionsStatistics = value;
        }

        void setFreeRegionsCount(int value) {
            freeRegionsCount = value;
        }

        void setOldRegionsStatistics(int value) {
            oldRegionsStatistics = value;
        }

        void setOldRegionsCount(int value) {
            oldRegionsCount = value;
        }

        int getYoungRegionsStatistics() {
            return youngRegionsStatistics;
        }

        int getYoungRegionsCount() {
            return youngRegionsCount;
        }

        int getHumonguousRegionsStatistics() {
            return humonguousRegionsStatistics;
        }

        int getHumonguousRegionsCount() {
            return humonguousRegionsCount;
        }

        int getFreeRegionsStatistics() {
            return freeRegionsStatistics;
        }

        int getFreeRegionsCount() {
            return freeRegionsCount;
        }

        int getOldRegionsStatistics() {
            return oldRegionsStatistics;
        }

        int getOldRegionsCount() {
            return oldRegionsCount;
        }
    }
}
