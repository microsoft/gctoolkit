// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.shenandoah;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCEvent;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ShenandoahCycle extends GCEvent {

    private ERGO ergonomics;

    /**
     * @param timeStamp time of event
     * @param gcType type of event
     * @param cause reason for triggering event
     * @param duration duration of the event
     */
    public ShenandoahCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    /**
     *
     * @param timeStamp time of event
     * @param duration duration of the event
     */
    public ShenandoahCycle(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    /**
     *
     * @param timeStamp time of event
     * @param cause reason for triggering event
     * @param duration duration of the event
     */
    public ShenandoahCycle(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    /**
     *
     * @param timeStamp time of event
     * @param gcType type of event
     * @param duration duration of the event
     */
    public ShenandoahCycle(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }

    /**
     *
     * @deprecated use setErgonomics(...) instead
     * @param free memory after collection
     * @param maxFree max free memory
     * @param humongous allocations
     * @param fragExternal fragmented memory outside of heap
     * @param fragInternal fragmented memory inside heap
     * @param reserve currently reserved memory
     * @param maxReserve max reserved memory
     */
    @Deprecated(forRemoval = true)
    public void addErgonomics(int free, int maxFree, int humongous, double fragExternal, double fragInternal, int reserve, int maxReserve) {
        setErgonomics(free, maxFree, humongous, fragExternal, fragInternal, reserve, maxReserve);
    }

    /**
     *
     * @param free memory after collection
     * @param maxFree max free memory
     * @param humongous allocations
     * @param fragExternal fragmented memory outside of heap
     * @param fragInternal fragmented memory inside heap
     * @param reserve currently reserved memory
     * @param maxReserve max reserved memory
     */
    public void setErgonomics(int free, int maxFree, int humongous, double fragExternal, double fragInternal, int reserve, int maxReserve) {
        this.ergonomics = new ERGO(free, maxFree, humongous, fragExternal, fragInternal, reserve, maxReserve);
    }

    /**
     * @return the ergonomics
     */
    ERGO getErgonomics() {
        return ergonomics;
    }

    enum Phases {
        ConcurrentReset, Pause_Initial_Mark,
        Concurrent_Marking, Concurrent_precleaning,
        Pause_Final_Mark, Concurrent_cleanup,
        Concurrent_evacuation, Pause_Init_Update_Refs,
        Concurrent_update_references, Pause_Final_Update_Refs;
    }


    class Phase {

        Phases phase;
        int activeWorkerThreadCount,totalWorkerThreadCount;

        // Pacer for (Evacuation). Used CSet: 1368M, Free: 5744M, Non-Taxable: 574M, Alloc Tax Rate: 1.1x
        // duration

    }

    class ERGO {

        private final int free;
        private final int maxFree;
        private final int humongous;
        private final double fragExternal;
        private final double fragInternal;
        private final int reserve;
        private final int maxReserve;

        /**
         *
         * @param free memory after collection
         * @param maxFree max free memory
         * @param humongous allocations
         * @param fragExternal fragmented memory outside of heap
         * @param fragInternal fragmented memory inside heap
         * @param reserve currently reserved memory
         * @param maxReserve max reserved memory
         */
        public ERGO(int free, int maxFree, int humongous, double fragExternal, double fragInternal, int reserve, int maxReserve) {
            this.free = free;
            this.maxFree = maxFree;
            this.humongous = humongous;
            this.fragExternal = fragExternal;
            this.fragInternal = fragInternal;
            this.reserve = reserve;
            this.maxReserve = maxReserve;
        }

        /**
         *
         * @return free
         */
        public int getFree() {
            return free;
        }

        /**
         *
         * @return max free
         */
        public int getMaxFree() {
            return maxFree;
        }

        /**
         *
         * @return humongous count
         */
        public int getHumongous() {
            return humongous;
        }

        /**
         *
         * @return external fragmentation
         */
        public double getFragExternal() {
            return fragExternal;
        }

        /**
         *
         * @return internal fragmentation
         */
        public double getFragInternal() {
            return fragInternal;
        }

        /**
         *
         * @return reserve
         */
        public int getReserve() {
            return reserve;
        }

        /**
         *
         * @return return max reserve
         */
        public int getMaxReserve() {
            return maxReserve;
        }

    }

    /*

GCCause
"[0.876s][info][gc,ergo      ] Free: 7724M, Max: 4096K regular, 7724M humongous, Frag: 0% external, 0% internal; Reserve: 412M, Max: 4096K",
ConcurrentReset
    activeWorkerThreadCount,totalWorkerThreadCount
    Pacer for (Evacuation). Used CSet: 1368M, Free: 5744M, Non-Taxable: 574M, Alloc Tax Rate: 1.1x
    duration

Pause Initial Mark
    activeWorkerThreadCount,totalWorkerThreadCount
    Pacer for (Mark) Expected Live: 819M, Free: 7724M, Non-Taxable: 772M, Alloc Tax Rate: 0.4x"
    duration

Concurrent Marking
    activeWorkerThreadCount,totalWorkerThreadCount
    duration

Concurrent precleaning
    activeWorkerThreadCount,totalWorkerThreadCount
    Pacer for (Precleaning). Non-Taxable: 8192M",
    duration

Pause Final Mark
    activeWorkerThreadCount,totalWorkerThreadCount
    Cleaned string and symbol table, strings: 9281 processed, 0 removed, symbols: 68910 processed, 23 removed"
    Adaptive CSet Selection. Target Free: 1160M, Actual Free: 8128M, Max CSet: 341M, Min Garbage: 0B"
    Collectable Garbage: 48448K (100%), Immediate: 0B (0%), CSet: 48448K (100%)"
    Pacer for (Evacuation). Used CSet: 57344K, Free: 7716M, Non-Taxable: 771M, Alloc Tax Rate: 1.1x"
    duration

Concurrent cleanup
    64M->68M(8192M) 0.045ms"
    Free: 7712M, Max: 4096K regular, 7712M humongous, Frag: 0% external, 0% internal; Reserve: 411M, Max: 4096K"

Concurrent evacuation
    activeWorkerThreadCount,totalWorkerThreadCount
    duration

Pause Init Update Refs
    Pacer for (Update Refs). Used: 81920K, Free: 7712M, Non-Taxable: 771M, Alloc Tax Rate: 1.1x"
    duration

Concurrent update references
    activeWorkerThreadCount,totalWorkerThreadCount
    duration

Pause Final Update Refs
    activeWorkerThreadCount,totalWorkerThreadCount
    duration

Concurrent cleanup
    Concurrent cleanup 84M->28M(8192M) 0.039ms
    Free: 7752M, Max: 4096K regular, 7696M humongous, Frag: 1% external, 0% internal; Reserve: 412M, Max: 4096K",
    Metaspace: 20546K->20754K(1069056K)",
    Pacer for (Idle). Initial: 163M, Alloc Tax Rate: 1.0x"
 */

}

/*
                "[0.876s][info][gc           ] Trigger: Metadata GC Threshold",
                "[0.876s][info][gc,ergo      ] Free: 7724M, Max: 4096K regular, 7724M humongous, Frag: 0% external, 0% internal; Reserve: 412M, Max: 4096K",
                "[0.876s][info][gc,start     ] GC(0) Concurrent reset",
                "[0.876s][info][gc,task      ] GC(0) Using 2 of 4 workers for concurrent reset",
                "[0.876s][info][gc,ergo      ] GC(0) Pacer for Reset. Non-Taxable: 8192M",
                "[0.876s][info][gc           ] GC(0) Concurrent reset 0.252ms",
                "[0.877s][info][gc,start     ] GC(0) Pause Init Mark (process weakrefs) (unload classes)",
                "[0.877s][info][gc,task      ] GC(0) Using 4 of 4 workers for init marking",
                "[0.878s][info][gc,ergo      ] GC(0) Pacer for Mark. Expected Live: 819M, Free: 7724M, Non-Taxable: 772M, Alloc Tax Rate: 0.4x",
                "[0.878s][info][gc           ] GC(0) Pause Init Mark (process weakrefs) (unload classes) 1.692ms",
                "[0.878s][info][gc,start     ] GC(0) Concurrent marking (process weakrefs) (unload classes)",
                "[0.878s][info][gc,task      ] GC(0) Using 2 of 4 workers for concurrent marking",
                "[0.883s][info][gc           ] GC(0) Concurrent marking (process weakrefs) (unload classes) 4.315ms",
                "[0.883s][info][gc,start     ] GC(0) Concurrent precleaning",
                "[0.883s][info][gc,task      ] GC(0) Using 1 of 4 workers for concurrent preclean",
                "[0.883s][info][gc,ergo      ] GC(0) Pacer for Precleaning. Non-Taxable: 8192M",
                "[0.883s][info][gc           ] GC(0) Concurrent precleaning 0.232ms",
                "[0.883s][info][gc,start     ] GC(0) Pause Final Mark (process weakrefs) (unload classes)",
                "[0.883s][info][gc,task      ] GC(0) Using 4 of 4 workers for final marking",
                "[0.885s][info][gc,stringtable] GC(0) Cleaned string and symbol table, strings: 9281 processed, 0 removed, symbols: 68910 processed, 23 removed",
                "[0.886s][info][gc,ergo       ] GC(0) Adaptive CSet Selection. Target Free: 1160M, Actual Free: 8128M, Max CSet: 341M, Min Garbage: 0B",
                "[0.886s][info][gc,ergo       ] GC(0) Collectable Garbage: 48448K (100%), Immediate: 0B (0%), CSet: 48448K (100%)",
                "[0.886s][info][gc,ergo       ] GC(0) Pacer for Evacuation. Used CSet: 57344K, Free: 7716M, Non-Taxable: 771M, Alloc Tax Rate: 1.1x",
                "[0.886s][info][gc            ] GC(0) Pause Final Mark (process weakrefs) (unload classes) 3.175ms",
                "[0.886s][info][gc,start      ] GC(0) Concurrent cleanup",
                "[0.886s][info][gc            ] GC(0) Concurrent cleanup 64M->68M(8192M) 0.045ms",
                "[0.886s][info][gc,ergo       ] GC(0) Free: 7712M, Max: 4096K regular, 7712M humongous, Frag: 0% external, 0% internal; Reserve: 411M, Max: 4096K",
                "[0.886s][info][gc,start      ] GC(0) Concurrent evacuation",
                "[0.886s][info][gc,task       ] GC(0) Using 2 of 4 workers for concurrent evacuation",
                "[0.891s][info][gc            ] GC(0) Concurrent evacuation 4.539ms",
                "[0.891s][info][gc,start      ] GC(0) Pause Init Update Refs",
                "[0.891s][info][gc,ergo       ] GC(0) Pacer for Update Refs. Used: 81920K, Free: 7712M, Non-Taxable: 771M, Alloc Tax Rate: 1.1x",
                "[0.891s][info][gc            ] GC(0) Pause Init Update Refs 0.033ms",
                "[0.891s][info][gc,start      ] GC(0) Concurrent update references",
                "[0.891s][info][gc,task       ] GC(0) Using 2 of 4 workers for concurrent reference update",
                "[0.895s][info][gc            ] GC(0) Concurrent update references 4.072ms",
                "[0.895s][info][gc,start      ] GC(0) Pause Final Update Refs",
                "[0.895s][info][gc,task       ] GC(0) Using 4 of 4 workers for final reference update",
                "[0.896s][info][gc            ] GC(0) Pause Final Update Refs 0.271ms",
                "[0.896s][info][gc,start      ] GC(0) Concurrent cleanup",
                "[0.896s][info][gc            ] GC(0) Concurrent cleanup 84M->28M(8192M) 0.039ms",
                "[0.896s][info][gc,ergo       ] Free: 7752M, Max: 4096K regular, 7696M humongous, Frag: 1% external, 0% internal; Reserve: 412M, Max: 4096K",
                "[0.896s][info][gc,metaspace  ] Metaspace: 20546K->20754K(1069056K)",
                "[0.896s][info][gc,ergo       ] Pacer for Idle. Initial: 163M, Alloc Tax Rate: 1.0x"

 */
