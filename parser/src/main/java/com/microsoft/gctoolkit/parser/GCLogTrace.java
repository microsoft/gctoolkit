// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GCCauses;
import com.microsoft.gctoolkit.event.MemoryPoolSummary;
import com.microsoft.gctoolkit.event.RegionSummary;
import com.microsoft.gctoolkit.event.UnifiedCountSummary;
import com.microsoft.gctoolkit.event.UnifiedStatisticalSummary;
import com.microsoft.gctoolkit.event.jvm.MetaspaceRecord;
import com.microsoft.gctoolkit.event.jvm.PermGenSummary;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * Class that represents a chunk of GC log that we are attempting to match to a
 * known GC log pattern
 */
public class GCLogTrace extends AbstractLogTrace {

    private static final Logger LOGGER = Logger.getLogger(GCLogTrace.class.getName());

    private final boolean gcCauseDebugging = Boolean.getBoolean("microsoft.debug.gccause");
    private final boolean debugging = Boolean.getBoolean("microsoft.debug");

    public GCLogTrace(Matcher matcher) {
        super(matcher);
    }

    public int groupCount() {
        return trace.groupCount();
    }

    public boolean groupNotNull(int index) {
        return getGroup(index) != null;
    }

    public long getLongGroup(int index) {
        return Long.parseLong(trace.group(index));
    }

    public int getIntegerGroup(int index) {
        return Integer.parseInt(trace.group(index));
    }

    public GCCause gcCause(int base, int offset) {
        if (gcCauseDebugging) {
            if (this.getGroup(base + offset) != null) {
                LOGGER.fine("GC cause: " + getGroup(base + offset));
                notYetImplemented();
            }
        }
        return GCCauses.get(getGroup(base + offset));
    }

    public GCCause gcCause(int offset) {
        return gcCause(3, offset);
    }

    public GCCause gcCause() {
        return gcCause(3, 0);
    }

    public double getPauseTime() {
        return getDoubleGroup(groupCount());
    }

    public double getDuration() {
        return getDoubleGroup(groupCount());
    }

    public double getDurationInSeconds() {
        return getDuration() / 1000.00d;
    }

    /**
     * Annoyingly we're assuming the field actually is ms instead of confirming
     * @param index Index of the capture group.
     * @return The capture group parsed to a double.
     */
    public double getMilliseconds(int index) {
        return getDoubleGroup(index);
    }

    /**
     * Assumed to be the last capture group
     * @return The last capture group parsed to a double.
     */
    public double getMilliseconds() {
        return getMilliseconds(groupCount());
    }

    public boolean contains(int index, String value) {
        String text = getGroup(index);
        if (text != null)
            return text.contains(value);
        return false;
    }

    public boolean contains(String value) {
        return trace.group(0).contains(value);
    }

    @Override
    public String toString() {
        return trace.group(0);
    }

    public boolean hasNext() {
        return (trace.find());
    }

    public int end() {
        return trace.end();
    }

    public long getMemoryInKBytes(int offset) {
        return toKBytes(getLongGroup(offset), getGroup(offset + 1));
    }

    long toKBytes(long value, String units) {
        long returnValue = value;
        switch (Character.toUpperCase(units.codePointAt(0))) {
            case 'G':
                returnValue *= 1024L;
            case 'M':
                returnValue *= 1024L;
            case 'K':
                break;
            case 'B':
                returnValue /= 1024L;
                break;
            default:
                LOGGER.log(Level.WARNING, "Invalid unit [B,K,M,G] {0}", units);
        }

        return returnValue;
    }

    double toKBytes(double value, String units) {
        double returnValue = value;
        switch (Character.toUpperCase(units.codePointAt(0))) {
            case 'G':
                returnValue *= 1024.0D;
            case 'M':
                returnValue *= 1024.0D;
            case 'K':
                break;
            case 'B':
                returnValue /= 1024.0D;
                break;
            default:
                LOGGER.log(Level.WARNING, "Invalid unit [B,K,M,G] {0}", units);
        }
        return returnValue;
    }

    public PermGenSummary getMetaspaceSummary(int offset) {
        try {
            long before = getMemoryInKBytes(offset);
            long after = getMemoryInKBytes(offset + 2);
            long size = getMemoryInKBytes(offset + 4);
            return new PermGenSummary(before, after, size);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.fine("Unable to calculate Metaspace summary.");
            notYetImplemented();
        }
        return null;
    }

    public MemoryPoolSummary getOccupancyBeforeAfterWithMemoryPoolSizeSummary(int offset) {
        try {
            long before = getMemoryInKBytes(offset);
            long after = getMemoryInKBytes(offset + 2);
            long size = getMemoryInKBytes(offset + 4);
            return new MemoryPoolSummary(before, size, after, size);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.fine("Unable to calculate generational memory pool summary.");
            notYetImplemented();
        }

        return null;
    }

    public MemoryPoolSummary getOccupancyWithMemoryPoolSizeSummary(int offset) {

        try {
            long occupancy = getMemoryInKBytes(offset);
            long size = getMemoryInKBytes(offset + 2);
            return new MemoryPoolSummary(occupancy, size, occupancy, size);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.fine("Unable to calculate generational memory pool occupancy summary.");
            notYetImplemented();
        }

        return null;
    }

    public MetaspaceRecord getMetaSpaceRecord(int offset) {
        try {
            long before = getMemoryInKBytes(offset);
            long after = getMemoryInKBytes(offset + 2);
            long size = getMemoryInKBytes(offset + 4);
            return new MetaspaceRecord(before, after, size);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.fine("Unable to calculate Metaspace summary.");
            notYetImplemented();
        }
        return null;
    }

    public MetaspaceRecord getEnlargedMemoryPoolRecord(int offset) {
        try {
            long before = getMemoryInKBytes(offset);
            long after = getMemoryInKBytes(offset + 4);
            long size = getMemoryInKBytes(offset + 6);
            return new MetaspaceRecord(before, after, size);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.fine("Unable to calculate Metaspace summary.");
            notYetImplemented();
        }
        return null;
    }

    public MetaspaceRecord getEnlargedMetaSpaceRecord(int offset) {
        try {
            long before = getMemoryInKBytes(offset);
            long sizeBefore = getMemoryInKBytes(offset + 2);
            long after = getMemoryInKBytes(offset + 4);
            long size = getMemoryInKBytes(offset + 6);
            return new MetaspaceRecord(before, after, size);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.fine("Unable to calculate Metaspace summary.");
            notYetImplemented();
        }
        return null;
    }

    public UnifiedStatisticalSummary getUnifiedStatisticalSummary() {
        return new UnifiedStatisticalSummary(getDoubleGroup(2), getDoubleGroup(3), getDoubleGroup(4), getDoubleGroup(5), getDoubleGroup(6), getIntegerGroup(7));
    }

    public UnifiedCountSummary countSummary() {
        return new UnifiedCountSummary(getIntegerGroup(2), getDoubleGroup(3), getIntegerGroup(4), getIntegerGroup(5), getIntegerGroup(6), getIntegerGroup(7));
    }

    public RegionSummary regionSummary() {
        return new RegionSummary(getIntegerGroup(2),
                getIntegerGroup(3),
                (trace.group(4) != null) ? getIntegerGroup(4) : getIntegerGroup(3));
    }

    // Debugging support
    public void notYetImplemented() {
        String threadName = Thread.currentThread().getName();
        LOGGER.log(Level.FINE, "{0}, not implemented: {1}", new Object[]{threadName, getGroup(0)});
        for (int i = 1; i < groupCount() + 1; i++) {
            LOGGER.log(Level.FINE, "{0} : {1}", new Object[]{i, getGroup(i)});
        }
        LOGGER.fine("-----------------------------------------");
        //IntelliJ Eats this log output so it's displayed to stdout..
        //And yes, that means System.out.println is in here in on purpose
        //if ( debugging) {
        System.out.println(threadName + ", not implemented: " + getGroup(0));
        for (int i = 1; i < groupCount() + 1; i++) {
            System.out.println(i + ": " + getGroup(i));
        }
        System.out.println("-----------------------------------------");
        //}
    }
}
