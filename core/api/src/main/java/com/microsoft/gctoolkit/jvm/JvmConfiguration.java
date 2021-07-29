// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.jvm;

/**
 * The configuration of the Java Virtual Machine, calculated from the GC log analysis.
 */
public interface JvmConfiguration {
    /**
     * Was the JVM configured with {@code -XX:+PrintGCDetails}, or with {@code -Xlog:gc*}?
     * @return {@code true} if the JVM was configured to print GC details.
     */
    boolean isPrintGCDetails();

    /**
     * Was the JVM configured with {@code -XX:+PrintTenuringDistribution}, or with {@code -Xlog:gc+age*=trace}
     * @return {@code true} if the JVM was configured to print tenuring distribution details.
     */
    boolean isPrintTenuringDistribution();

    /**
     * Was the JVM configured with {@code -XX:+PrintGCApplicationConcurrentTime},
     * {@code -XX:+PrintGCApplicationConcurrentTime}, or with {@code -Xlog:safepoint}?
     * @return {@code true} if the JVM was configured to log time spent in safepoints.
     */
    boolean hasSafepointEvents();

    /**
     * Was the {@code MaxTenuringThreshold} garbage collection option set to a value greater than 15? Prior to
     * Java version 1.5.0_06, the maximum value was 31. From version 1.5.0_06 on, a value greater than 15 means
     * that objects never tenure, which is likely to cause heap fragmentation once survivor space is filled.
     * The method {@link #getMaxTenuringThreshold()}} can be used to retrieve the value.
     * @return {@code true} if {@code MaxTenuringThreshold} is set with a value greater than 15.
     */
    boolean hasMaxTenuringThresholdViolation();

    /**
     * Get the value of  the {@code MaxTenuringThreshold} garbage collection option.
     * @return The value of the {@code MaxTenuringThreshold} garbage collection option.
     */
    int getMaxTenuringThreshold();

    /**
     * Return {@code true} if the JDK version has been determined from parsing the log file. This method
     * should be used in conjunction with {@link #isJDK70()}, {@link #isJDK80()}}, and {@link #isPreJDK17040()}
     * to determine whether these methods return {@code false} because the parser could not determine the JDK version.
     * @return {@code} true if the JDK version is known.
     */
    boolean isJDKVersionKnown();

    /**
     * Is the version of Java 1.7 prior to 7u40? Up until 170_40, the only GC Cause printed was System.gc(),
     * and only with +PrintGCDetails.
     * @return {@code true} if the Java version is 1.7 prior to 7u40.
     */
    boolean isPreJDK17040();

    /**
     * Is the version of Java 1.7 after, and including, 7u40? The {@code PrintGCCause} flag was added in 7u40, and
     * was necessary to include {@code System.gc()}.
     * @return {@code true} if the Java version is 1.7 after, and including, 7u40.
     */
    boolean isJDK70();

    /**
     * Is the version of Java 1.8? In version 1.8, {@code PrintGCDetails} includes {@code System.gc()}.
     * @return {@code true} if the version of Java is 1.8.
     */
    boolean isJDK80();

    /**
     * Do the JVM arguments include {@code -XX:+PrintGCCause}? If {@link #isJDK70()} returns {@code true} and
     * {@code containsGCCause()} returns {@code false}, then {@code System.gc()} calls are not being logged.
     * @return {@code true} if the JVM is configured to print GC cause.
     */
    boolean containsGCCause();

}
