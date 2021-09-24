// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

import com.microsoft.gctoolkit.event.CPUSummary;
import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import com.microsoft.gctoolkit.parser.jvm.Decorators;

public class ForwardReference {

    private final Decorators decorators;
    private final int gcID;
    private DateTimeStamp startTime = null;
    private double duration = -1.0d;
    private GCCause gcCause = GCCause.UNKNOWN_GCCAUSE;
    private CPUSummary cpuSummary = null;

    public ForwardReference(Decorators decorators, int id) {
        this.gcID = id;
        this.decorators = decorators;
    }

    public int getGcID() {
        return gcID;
    }

    void setStartTime(DateTimeStamp startTime) {
        this.startTime = startTime;
    }

    DateTimeStamp getStartTime() {
        return startTime;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    double getDuration() {
        return duration;
    }

    public void setGCCause(GCCause cause) {
        this.gcCause = cause;
    }

    GCCause getGCCause() {
        return this.gcCause;
    }

    void setCPUSummary(CPUSummary summary) {
        this.cpuSummary = summary;
    }

    CPUSummary getCPUSummary() {
        return this.cpuSummary;
    }

    public void add(CPUSummary cpuSummary) {
        this.cpuSummary = cpuSummary;
    }
}
