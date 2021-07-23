// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.vertx.jvm;

import com.microsoft.censum.parser.jvm.JVMConfiguration;

/* package scope */ class JvmConfigurationImpl implements com.microsoft.censum.jvm.JvmConfiguration {

    private final JVMConfiguration jvmConfiguration;
    /* package scope */
    JvmConfigurationImpl(JVMConfiguration jvmConfiguration) {
        this.jvmConfiguration = jvmConfiguration;
    }

    @Override
    public boolean isPrintGCDetails() {
        return jvmConfiguration.getDiary().isPrintGCDetails();
    }

    @Override
    public boolean isPrintTenuringDistribution() {
        return jvmConfiguration.getDiary().isTenuringDistribution();
    }

    @Override
    public boolean hasSafepointEvents() {
        return jvmConfiguration.getDiary().isApplicationStoppedTime()
                || jvmConfiguration.getDiary().isApplicationRunningTime();
    }

    @Override
    public boolean hasMaxTenuringThresholdViolation() {
        return jvmConfiguration.getDiary().isMaxTenuringThresholdViolation();
    }

    @Override
    public int getMaxTenuringThreshold() {
        return jvmConfiguration.getMaxTenuringThreshold();
    }

    @Override
    public boolean isPreJDK17040() {
        return jvmConfiguration.getDiary().isPre70_40();
    }

    @Override
    public boolean isJDK70() {
        return jvmConfiguration.getDiary().isJDK70();
    }

    @Override
    public boolean isJDK80() {
        return jvmConfiguration.getDiary().isJDK80();
    }

    @Override
    public boolean containsGCCause() {
        return jvmConfiguration.getDiary().isGCCause();
    }

    @Override
    public boolean isJDKVersionKnown() { return jvmConfiguration.getDiary().isVersionKnown(); }
}
