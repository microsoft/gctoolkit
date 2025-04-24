package com.microsoft.gctoolkit.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.microsoft.gctoolkit.event.g1gc.G1Mixed;
import com.microsoft.gctoolkit.event.g1gc.G1Young;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;
import com.microsoft.gctoolkit.jvm.Diarizer;
import com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer;

public class PreUnifiedG1GCParserTest extends ParserTest {

    protected Diarizer diarizer() {
        return new PreUnifiedDiarizer();
    };

    protected GCLogParser parser() {
        return new PreUnifiedG1GCParser();
    };

    @Test
    // jlittle-ptc: Added to validate changes in https://github.com/microsoft/gctoolkit/issues/433
    // Fails without changes, passes with changes.
    public void testJava8PreUnifiedG1GCYoungEvents() {
    	String[] lines = new String[] {
    		    "0.867: [GC pause (G1 Evacuation Pause) (young) 52816K->9563K(1024M), 0.0225122 secs]",
    		    "5.478: [GC pause (young) 8878K->5601K(13M), 0.0027650 secs]",
    		    "1566.108: [GC pause (mixed) 7521K->5701K(13M), 0.0030090 secs]",
    			"1834339.155: [GC pause (G1 Evacuation Pause) (mixed) 309M->141M(1111M), 0.0188779 secs]"
    	};

    	List<JVMEvent> jvmEvents = feedParser(lines);
    	assertEquals(4, jvmEvents.size());
    	
    	// First two lines are G1Young, followed by G1Mixed
    	assertMemoryPoolValues(((G1Young) jvmEvents.get(0)).getHeap(), 52816, 1024*1024, 9563, 1024*1024);
    	assertMemoryPoolValues(((G1Young) jvmEvents.get(1)).getHeap(), 8878, 13*1024, 5601, 13*1024);
    	
    	assertMemoryPoolValues(((G1Mixed) jvmEvents.get(2)).getHeap(), 7521, 13*1024, 5701, 13*1024);
    	assertMemoryPoolValues(((G1Mixed) jvmEvents.get(3)).getHeap(), 309*1024, 1111*1024, 141*1024, 1111*1024);
    	
    }

	
}
