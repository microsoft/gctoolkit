// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.parser.unified;

import com.microsoft.censum.parser.GCParseRule;

public interface ShenandoahPatterns extends UnifiedPatterns {

    GCParseRule SHENANDOAH_TAG = new GCParseRule("Shenandoah Tag", "Using Shenandoah");

    GCParseRule CONCURRENT = new GCParseRule("CONCURRENT","Concurrent (reset|marking|cleanup|evacuation|update references|cleanup)( " + DURATION_MS + ")?$");
    GCParseRule CLEANUP = new GCParseRule( "CLEANUP", "Concurrent cleanup " + BEFORE_AFTER_CONFIGURED_PAUSE);
    GCParseRule WORKERS = new GCParseRule( "WORKERS", "Using (\\d+) of (\\d+) workers for (concurrent reset|init marking|concurrent marking|final marking|concurrent evacuation|concurrent reference update|final reference update)");
    GCParseRule PAUSE = new GCParseRule( "PAUSE", "Pause (Init Mark|Final Mark|Init Update Refs|Final Update Refs)( " + PAUSE_TIME + ")?$");
    GCParseRule PACER = new GCParseRule( "PACER", "Pacer for (Reset|Mark|Evacuation|Update Refs|Idle)");
    GCParseRule CSET_SELECTION = new GCParseRule("CSET_SELECTION","Adaptive CSet Selection. Target Free: " + MEMORY_SIZE + ", Actual Free: " + MEMORY_SIZE + ", Max CSet: " + MEMORY_SIZE + ", Min Garbage: " + MEMORY_SIZE);
    GCParseRule COLLECTABLE = new GCParseRule("COLLECTABLE","Collectable Garbage: " + MEMORY_SIZE + " \\((\\d+)%\\), Immediate: " + MEMORY_SIZE + " \\((\\d+)%\\), CSet: " + MEMORY_SIZE + " \\((\\d+)%\\)");
    GCParseRule FREE = new GCParseRule("FREE","Free: (\\d+)(B|K|M|G), Max: " + MEMORY_SIZE + " regular, " + MEMORY_SIZE + " humongous, Frag: (\\d+)% external, (\\d+)% internal; Reserve: " + MEMORY_SIZE + ", Max: " + MEMORY_SIZE);
    GCParseRule METASPACE = new GCParseRule("METASPACE","Metaspace: " + BEFORE_AFTER_CONFIGURED);
    GCParseRule TRIGGER = new GCParseRule("TRIGGER","Trigger: ");
    GCParseRule HEADROOM = new GCParseRule("HEADROOM","Free headroom: " + MEMORY_SIZE + " \\(free\\) - " + MEMORY_SIZE + " \\(spike\\) - " + MEMORY_SIZE + " \\(penalties\\) = " + MEMORY_SIZE);
    GCParseRule INFO = new GCParseRule("INFO","Min heap equals to max heap, disabling ShenandoahUncommit");
    GCParseRule ADVICE1 = new GCParseRule("ADVICE1","Consider -XX:\\+ClassUnloadingWithConcurrentMark if large pause times are observed on class-unloading sensitive workloads");
    GCParseRule REGION = new GCParseRule("REGION","Regions: (\\d+) x (\\d+)K");
    GCParseRule HUMONGOUS_OBJECT_THRESHOLD = new GCParseRule("HUMONGOUS_OBJECT_THRESHOLD","Humongous object threshold: (\\d+)K");
    GCParseRule TLAB = new GCParseRule("TLAB","Max TLAB size: (\\d+)K");
    GCParseRule THREADS = new GCParseRule("THREADS","GC threads: (\\d+) parallel, (\\d+) concurrent");
    GCParseRule MODE = new GCParseRule("MODE","Shenandoah (GC mode|heuristics): (.+)");
    GCParseRule HEURISTICS = new GCParseRule("HEURISTICS","Heuristics ergonomically sets (.+)");
    GCParseRule INIT = new GCParseRule("INIT","Initialize Shenandoah heap: (\\d+)M initial, (\\d+)M min, (\\d+)M max");
    GCParseRule SAFEPOINTING = new GCParseRule("SAFEPOINTING","Safepointing mechanism: (.+)");
    GCParseRule HEAP = new GCParseRule("HEAP","Heap address: " + HEX + ", size: (\\d+) MB, Compressed Oops mode: (.+)");
    GCParseRule REFERENCE = new GCParseRule("REFERENCE","Reference processing: (.+)");

}
