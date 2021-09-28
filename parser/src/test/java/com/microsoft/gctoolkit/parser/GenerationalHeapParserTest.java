// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.generational.FullGC;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.parser.jvm.LoggingDiary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class GenerationalHeapParserTest {


    @Test
    public void canParseLFSFullGC() {

        String fragment = "2019-11-14T23:50:29.896+0000: 91334.028: [Full GC (Allocation Failure) Before GC:\n" +
                "Statistics for BinaryTreeDictionary:\n" +
                "------------------------------------\n" +
                "Total Free Space: 1261\n" +
                "Max   Chunk Size: 1261\n" +
                "Number of Blocks: 1\n" +
                "Av.  Block  Size: 1261\n" +
                "Tree      Height: 1\n" +
                "2019-11-14T23:50:29.896+0000: 91334.028: [CMSCMS: Large block 0x00000007bfffd898\n" +
                ": 2097142K->2097142K(2097152K), 1.9092744 secs] 4063215K->4063215K(4063232K), [Metaspace: 99441K->99441K(1140736K)]After GC:\n" +
                "Statistics for BinaryTreeDictionary:\n" +
                "------------------------------------\n" +
                "Total Free Space: 1261\n" +
                "Max   Chunk Size: 1261\n" +
                "Number of Blocks: 1\n" +
                "Av.  Block  Size: 1261\n" +
                "Tree      Height: 1\n" +
                ", 1.9094806 secs] [Times: user=1.91 sys=0.00, real=1.91 secs]\n" +
                "2019-11-14T23:50:31.806+0000: 91335.938: Total time for which application threads were stopped: 4.0762194 seconds, Stopping threads took: 0.0000522 seconds\n";

        AtomicBoolean eventCreated = new AtomicBoolean(false);
        GenerationalHeapParser parser = new GenerationalHeapParser(new LoggingDiary(), event -> {
            Assertions.assertTrue(event instanceof FullGC);
            FullGC fgc = (FullGC) event;
            Assertions.assertEquals(1.9094806d, fgc.getDuration());
            Assertions.assertEquals(4063232, fgc.getHeap().getSizeAfterCollection());
            eventCreated.set(true);
        });

        Arrays.stream(fragment.split("\n")).forEach(parser::receive);

        Assertions.assertTrue(eventCreated.get());

    }
}
