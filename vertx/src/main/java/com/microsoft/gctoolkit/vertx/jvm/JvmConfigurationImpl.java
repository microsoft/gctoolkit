// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.vertx.jvm;

import com.microsoft.gctoolkit.jvm.Diary;
import com.microsoft.gctoolkit.time.DateTimeStamp;

/* package scope */ class JvmConfigurationImpl implements com.microsoft.gctoolkit.jvm.JvmConfiguration {

    private final Diary diary;
    /* package scope */
    JvmConfigurationImpl(Diary diary) {
        this.diary = diary;
    }

    @Override
    public boolean isPrintGCDetails() {
        return diary.isPrintGCDetails();
    }

    @Override
    public boolean isPrintTenuringDistribution() {
        return diary.isTenuringDistribution();
    }

    @Override
    public boolean hasSafepointEvents() {
        return diary.isApplicationStoppedTime() || diary.isApplicationRunningTime();
    }

    @Override
    public boolean hasMaxTenuringThresholdViolation() {
        return diary.isMaxTenuringThresholdViolation();
    }

    @Override
    public int getMaxTenuringThreshold() {
        return 0; //diary.getMaxTenuringThreshold(); todo: add in command line values.
    }

    @Override
    public boolean isPreJDK17040() {
        return diary.isPre70_40();
    }

    @Override
    public boolean isJDK70() {
        return diary.isJDK70();
    }

    @Override
    public boolean isJDK80() {
        return diary.isJDK80();
    }

    @Override
    public boolean containsGCCause() {
        return diary.isGCCause();
    }

    @Override
    public String getCommandLine() {
        return null;
    }

    @Override
    public DateTimeStamp getTimeOfFirstEvent() {
        return null;
    }

    @Override
    public boolean isUnified() {
        return false;
    }

    @Override
    public Diary getDiary() {
        return null;
    }

    @Override
    public boolean hasJVMEvents() {
        return false;
    }

    @Override
    public boolean completed() {
        return false;
    }

    @Override
    public void fillInKnowns() {

    }

    @Override
    public boolean diarize(String line) {
        return false;
    }

    @Override
    public boolean isJDKVersionKnown() { return diary.isVersionKnown(); }
}
