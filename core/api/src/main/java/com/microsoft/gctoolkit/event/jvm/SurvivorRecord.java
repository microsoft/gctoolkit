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
    // Fold anything older than 15 back into the 15th slot
    private ArrayList<Long> bytesAtAge = null;

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
        return this.bytesAtAge.get(age);
    }

    /*
     * There is a bug in the JVM that allows tenuring threshold to appear to be greater than 15.
     * Fold anything greater than 15 into 15.
     *
     */
    public void add(int age, long bytes) {

        if (bytesAtAge == null) {
            bytesAtAge = new ArrayList<>();
            bytesAtAge.add(0L); //throw away the first slow.
        }

        if (age <= maxTenuringThreshold) {
            bytesAtAge.add(bytes);
        } else {
            bytesAtAge.set(maxTenuringThreshold, bytesAtAge.get(maxTenuringThreshold) + bytes);
        }
    }

    public Long[] getBytesAtEachAge() {
        if (bytesAtAge == null)
            return new Long[0];
        return bytesAtAge.toArray(new Long[0]);
    }
}
