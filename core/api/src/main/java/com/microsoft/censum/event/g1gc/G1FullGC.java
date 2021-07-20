// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

import java.util.HashMap;
import java.util.stream.Stream;

public class G1FullGC extends G1RealPause {

    HashMap<String, Double> internalPhaseTimes = new HashMap<>();

    public G1FullGC(DateTimeStamp timeStamp, GCCause cause, double pauseTime) {
        this(timeStamp, GarbageCollectionTypes.Full, cause, pauseTime);
    }

    public G1FullGC(DateTimeStamp timeStamp, GarbageCollectionTypes type, GCCause cause, double pauseTime) {
        super(timeStamp, type, cause, pauseTime);
    }

    public boolean isFull() {
        return true;
    }

    public void addInternalPhase(String internalPhase, Double duration) {
        internalPhaseTimes.put(internalPhase, duration);
    }

    public Stream<String> internalPhases() {
        return internalPhaseTimes.keySet().stream();
    }

    public double internalPhaseDuration(String phaseName) {
        return internalPhaseTimes.get(phaseName);
    }
}
