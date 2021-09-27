// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event;

import com.microsoft.gctoolkit.time.DateTimeStamp;

/**
 * Exemplar
 * 11906.844: [GC pause (young)11906.881: [SoftReference, 0 refs, 0.0000060 secs]11906.881: [WeakReference, 0 refs, 0.0000020 secs]11906.881: [FinalReference, 0 refs, 0.0000010 secs]11906.881: [PhantomReference, 0 refs, 0.0000020 secs]11906.881: [JNI Weak Reference, 0.0002710 secs], 0.03831600 secs]
 */
public class ReferenceGCSummary {

    private DateTimeStamp softReferenceDateTimeStamp;
    private int softReferenceCount;
    private double softReferencePauseTime;

    private DateTimeStamp weakReferenceDateTimeStamp;
    private int weakReferenceCount;
    private double weakReferencePauseTime;

    private DateTimeStamp finalReferenceDateTimeStamp;
    private int finalReferenceCount;
    private double finalReferencePauseTime;

    private DateTimeStamp phantomReferenceDateTimeStamp;
    private int phantomReferenceCount;
    private int phantomReferenceFreedCount;
    private double phantomReferencePauseTime;

    private DateTimeStamp jniWeakReferenceDateTimeStamp;
    private int jniWeakReferenceCount;
    private double jniWeakReferencePauseTime;

    public ReferenceGCSummary() {
    }

    public DateTimeStamp getSoftReferenceDateTimeStamp() {
        return softReferenceDateTimeStamp;
    }

    public int getSoftReferenceCount() {
        return softReferenceCount;
    }

    public double getSoftReferencePauseTime() {
        return softReferencePauseTime;
    }

    public DateTimeStamp getWeakReferenceDateTimeStamp() {
        return weakReferenceDateTimeStamp;
    }

    public int getWeakReferenceCount() {
        return weakReferenceCount;
    }

    public double getWeakReferencePauseTime() {
        return weakReferencePauseTime;
    }

    public DateTimeStamp getFinalReferenceDateTimeStamp() {
        return finalReferenceDateTimeStamp;
    }

    public int getFinalReferenceCount() {
        return finalReferenceCount;
    }

    public double getFinalReferencePauseTime() {
        return finalReferencePauseTime;
    }

    public DateTimeStamp getPhantomReferenceDateTimeStamp() {
        return phantomReferenceDateTimeStamp;
    }

    public int getPhantomReferenceCount() {
        return phantomReferenceCount;
    }

    public int getPhantomReferenceFreedCount() {
        return phantomReferenceFreedCount;
    }

    public double getPhantomReferencePauseTime() {
        return phantomReferencePauseTime;
    }

    public DateTimeStamp getJniWeakReferenceDateTimeStamp() {
        return jniWeakReferenceDateTimeStamp;
    }

    public double getJniWeakReferencePauseTime() {
        return jniWeakReferencePauseTime;
    }

    public int getJniWeakReferenceCount() {
        return jniWeakReferenceCount;
    }

    public void addSoftReferences(DateTimeStamp dateTimeStamp, int count, double pauseTime) {
        softReferenceDateTimeStamp = dateTimeStamp;
        softReferenceCount = count;
        softReferencePauseTime = pauseTime;
    }

    public void addWeakReferences(DateTimeStamp dateTimeStamp, int count, double pauseTime) {
        weakReferenceDateTimeStamp = dateTimeStamp;
        weakReferenceCount = count;
        weakReferencePauseTime = pauseTime;
    }

    public void addFinalReferences(DateTimeStamp dateTimeStamp, int count, double pauseTime) {
        finalReferenceDateTimeStamp = dateTimeStamp;
        finalReferenceCount = count;
        finalReferencePauseTime = pauseTime;
    }

    public void addPhantomReferences(DateTimeStamp dateTimeStamp, int count, int freed, double pauseTime) {
        phantomReferenceDateTimeStamp = dateTimeStamp;
        phantomReferenceCount = count;
        phantomReferenceFreedCount = freed;
        phantomReferencePauseTime = pauseTime;
    }

    public void addPhantomReferences(DateTimeStamp dateTimeStamp, int count, double pauseTime) {
        addPhantomReferences(dateTimeStamp, count, 0, pauseTime);
    }

    public void addJNIWeakReferences(DateTimeStamp dateTimeStamp, double pauseTime) {
        jniWeakReferenceDateTimeStamp = dateTimeStamp;
        jniWeakReferenceCount = -1;
        jniWeakReferencePauseTime = pauseTime;
    }

    public void addJNIWeakReferences(DateTimeStamp dateTimeStamp, int count, double pauseTime) {
        jniWeakReferenceDateTimeStamp = dateTimeStamp;
        jniWeakReferenceCount = count;
        jniWeakReferencePauseTime = pauseTime;
    }
}
