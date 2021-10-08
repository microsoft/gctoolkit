// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * 2015-10-17T21:10:23.673-0400: 3993.137: [GC concurrent-string-deduplication, 79.3K-&gt;792.0B(78.6K), avg 96.2%, 0.0024351 secs]
 */

public class G1ConcurrentStringDeduplication extends G1GCConcurrentEvent {

    private double startingStringVolume;
    private double endingStringVolume;
    private double reduction;
    private double percentReduction;

    public G1ConcurrentStringDeduplication(DateTimeStamp timeStamp, double startingStringVolume, double endingStringVolume, double reduction, double percentReduction, double duration) {
        this(timeStamp, GCCause.GCCAUSE_NOT_SET, startingStringVolume, endingStringVolume, reduction, percentReduction, duration);
    }

    public G1ConcurrentStringDeduplication(DateTimeStamp timeStamp, GCCause cause, double startingStringVolume, double endingStringVolume, double reduction, double percentReduction, double duration) {
        super(timeStamp, GarbageCollectionTypes.ConcurrentStringDeduplication, cause, duration);
        this.startingStringVolume = startingStringVolume;
        this.endingStringVolume = endingStringVolume;
        this.reduction = reduction;
        this.percentReduction = percentReduction;
    }

    public double getStartingStringVolume() {
        return startingStringVolume;
    }

    public double getEndingStringVolume() {
        return endingStringVolume;
    }

    public double getReduction() {
        return reduction;
    }

    public double getPercentReduction() {
        return percentReduction;
    }


}
