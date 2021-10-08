// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.collection;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MRUQueueTest {

    @Test
    public void testMRUQueueOrdering() {

        String[] original = {"B", "A", "C"};
        String[] afterA = {"A", "B", "C"};
        String[] afterC = {"C", "A", "B"};

        MRUQueue<String, String> queue = new MRUQueue<>();
        queue.put("B", "E");
        queue.put("A", "D");
        queue.put("C", "F");

        assertEquals(3, queue.size());
        int i = 0;
        for (String key : queue.keys()) {
            assertEquals(key, original[i++]);
        }

        queue.get("A");
        assertEquals(3, queue.size());
        i = 0;
        for (String key : queue.keys()) {
            assertEquals(key, afterA[i++]);
        }

        queue.get("C");
        assertEquals(3, queue.size());
        i = 0;
        for (String key : queue.keys()) {
            assertEquals(key, afterC[i++]);
        }

    }
}
