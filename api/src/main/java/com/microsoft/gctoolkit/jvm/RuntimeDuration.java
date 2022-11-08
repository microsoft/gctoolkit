// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;
import com.microsoft.gctoolkit.time.DateTimeStamp;

@Collates(RuntimeDurationAggregator.class)
/* package-scope */ class RuntimeDuration implements Aggregation {
    private volatile DateTimeStamp timeOfFirstEvent = null;
    private volatile DateTimeStamp timeOfLastEvent = new DateTimeStamp(0.0d);
    /**
     * Runtime duration (in milliseconds) after which we no longer consider the GC log to be a fragment
     */
    private static final double LOG_FRAGMENT_THRESHOLD = 18000;

    public double getRuntimeDuration() {
        boolean isLogFragment = getTimeOfFirstEvent().getTimeStamp() > LOG_FRAGMENT_THRESHOLD;
        return isLogFragment
                ? getTimeOfLastEvent().minus(getTimeOfFirstEvent())
                : getTimeOfLastEvent().getTimeStamp();
    }

    public DateTimeStamp getTimeOfFirstEvent() {
        return timeOfFirstEvent != null ? timeOfFirstEvent : new DateTimeStamp(0.0);
    }

    public DateTimeStamp getTimeOfLastEvent() {
        if (getTimeOfFirstEvent().before(timeOfLastEvent))
            return timeOfLastEvent;
        else
            return getTimeOfFirstEvent();
    }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public void record(DateTimeStamp eventTime, double eventDuration) {
        DateTimeStamp now = eventTime.add(eventDuration);
        if (timeOfFirstEvent == null) {
            timeOfFirstEvent = now;
        }
        if (now.after(timeOfLastEvent)) {
            timeOfLastEvent = now;
        }

    }
}
