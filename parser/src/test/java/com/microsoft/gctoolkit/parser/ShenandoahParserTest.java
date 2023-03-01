// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.event.shenandoah.ShenandoahCycle;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;
import com.microsoft.gctoolkit.parser.patterns.ParserTest;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class ShenandoahParserTest extends ParserTest {

    @Override
    protected Diarizer diarizer() {
        return new UnifiedDiarizer();
    }

    @Override
    protected GCLogParser parser() {
        return new ShenandoahParser();
    }


    //todo: support for Shenandoah still under construction
    //@Test
    public void infoLevelShenandoahCycle() {


        String[] eventLogEntries = {
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
        };

        List<JVMEvent> singleCycle = feedParser(eventLogEntries);
        try {
            Assertions.assertTrue(singleCycle.size() == 1);
            ShenandoahCycle sc = (ShenandoahCycle) singleCycle.get(0);
            // todo: Put in checks for values
            //Memory

        } catch (Throwable t) {
            Assertions.fail(t);
        }
    }
}
