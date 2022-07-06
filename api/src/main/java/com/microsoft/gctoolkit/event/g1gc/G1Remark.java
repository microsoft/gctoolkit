// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;


public class G1Remark extends G1RealPause {

    private double referenceProcessingTime = 0.0d;
    private double finalizeMarkingTime = 0.0d;
    private double unloadingTime = 0.0d;

    public G1Remark(DateTimeStamp timeStamp, double referenceProcessingTimes, double duration) {
        super(timeStamp, GarbageCollectionTypes.G1GCRemark, GCCause.UNKNOWN_GCCAUSE, duration);
        this.referenceProcessingTime = referenceProcessingTimes;
    }

    public G1Remark(DateTimeStamp timeStamp, double referenceProcessingTimes, double finalizeMarking, double unloading, double duration) {
        this(timeStamp, referenceProcessingTimes, duration);
        this.finalizeMarkingTime = finalizeMarking;
        this.unloadingTime = unloading;
    }

    public double getReferenceProcessingTime() {
        return this.referenceProcessingTime;
    }

    public double getFinalizeMarkingTime() {
        return this.finalizeMarkingTime;
    }

    public double getUnloadingTime() {
        return this.unloadingTime;
    }

}
