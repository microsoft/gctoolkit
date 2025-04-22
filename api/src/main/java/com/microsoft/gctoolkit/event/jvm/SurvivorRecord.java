// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.jvm;

import com.microsoft.gctoolkit.time.DateTimeStamp;

import java.util.ArrayList;


public class SurvivorRecord extends JVMEvent {

    private static final int THEORETICAL_MAX_TENURING_THRESHOLD = 15;

    private final long desiredOccupancyAfterCollection;
    private int calculatedTenuringThreshold;
    private int maxTenuringThreshold;

    // JDK bug, we have now seen a max tenuring threshold of 32, even 64
    // Fold anything older than maxTenuringThreshold back into the maxTenuringThreshold slot
    private long[] bytesAtAge;

    public SurvivorRecord(DateTimeStamp timeStamp, long desiredOccupancy, int calculatedTenuringThreshold, int maxTenuringThreshold) {
        super(timeStamp, 0.0d);
        this.desiredOccupancyAfterCollection = desiredOccupancy;
        setCalculatedThreshold(calculatedTenuringThreshold);
        setMaxTenuringThreshold(maxTenuringThreshold);
    }

    private void setMaxTenuringThreshold(int maxTenuringThresholdFromLog) {
        if (maxTenuringThresholdFromLog <= THEORETICAL_MAX_TENURING_THRESHOLD) {
            this.maxTenuringThreshold = maxTenuringThresholdFromLog;
        } else {
            this.maxTenuringThreshold = THEORETICAL_MAX_TENURING_THRESHOLD;
        }
    }

    private void setCalculatedThreshold(int calculatedTenuringThresholdFromLog) {
        if (calculatedTenuringThresholdFromLog <= THEORETICAL_MAX_TENURING_THRESHOLD) {
            this.calculatedTenuringThreshold = calculatedTenuringThresholdFromLog;
        } else {
            this.calculatedTenuringThreshold = THEORETICAL_MAX_TENURING_THRESHOLD;
        }
    }

    public long getDesiredOccupancyAfterCollection() {
        return this.desiredOccupancyAfterCollection;
    }

    public int getCalculatedTenuringThreshold() {
        return this.calculatedTenuringThreshold;
    }

    public int getMaxTenuringThreshold() {
        return this.maxTenuringThreshold;
    }

    public long getBytesAtAge(int age) {
        if (this.bytesAtAge == null) return 0L;
        return this.bytesAtAge[normalizeAge(age)];
    }

    /*
     * There is a bug in the JVM that allows tenuring threshold to appear to be greater than 15.
     * Fold anything greater than maxTenuringThreshold into maxTenuringThreshold.
     */
    public void add(int age, long bytes) {

        if (bytesAtAge == null) {
            bytesAtAge = new long[maxTenuringThreshold+1];
            bytesAtAge[0] = 0L; //throw away the first slot.
        }

        if (age <= maxTenuringThreshold) {
            bytesAtAge[age] = bytes;
        } else {
            bytesAtAge[maxTenuringThreshold] += bytes;
        }
    }

    public long[] getBytesAtEachAge() {
        if (bytesAtAge == null)
            return new long[0];
        return bytesAtAge;
    }

    private int normalizeAge(int age) {
        return age <= maxTenuringThreshold ? age : maxTenuringThreshold;
    }
}
