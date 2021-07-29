// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.test.collection;


import com.microsoft.gctoolkit.parser.collection.MRUQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MRUQueueTest {

    @Test
    public void testMRUQueueOrdering() {

        String[] original = {"B", "A", "C"};
        String[] afterA = {"A", "B", "C"};
        String[] afterC = {"C", "A", "B"};

        MRUQueue<String, String> queue = new MRUQueue<String, String>();
        queue.put("B", "E");
        queue.put("A", "D");
        queue.put("C", "F");

        assertTrue(queue.size() == 3);
        int i = 0;
        for (String key : queue.keys()) {
            assertTrue(key.equals(original[i++]));
        }

        queue.get("A");
        assertTrue(queue.size() == 3);
        i = 0;
        for (String key : queue.keys()) {
            assertTrue(key.equals(afterA[i++]));
        }

        queue.get("C");
        assertTrue(queue.size() == 3);
        i = 0;
        for (String key : queue.keys()) {
            assertTrue(key.equals(afterC[i++]));
        }

    }
}
