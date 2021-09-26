// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser.unified;


public interface UnifiedLoggingCMSPatterns extends UnifiedLoggingTokens {

        /*
    [0.072s][info   ][gc] Using Concurrent Mark Sweep
[0.246s][info   ][gc,start] GC(0) Pause Young (Allocation Failure) (0.246s)
[1.169s][info   ][gc,heap ] GC(0) ParNew: 34944K->4351K(39296K)
[1.169s][info   ][gc,heap ] GC(0) CMS: 0K->29664K(87424K)
[1.169s][info   ][gc,metaspace] GC(0) Metaspace: 2982K->2982K(1056768K)
[1.169s][info   ][gc          ] GC(0) Pause Young (Allocation Failure) 34M->33M(123M) (0.246s, 1.169s) 922.681ms
[1.169s][info   ][gc,cpu      ] GC(0) User=4.60s Sys=0.26s Real=0.92s
     */

//    public static final String DECIMAL_POINT = "(?:\\.|,)";
//    public static final String INTEGER = "\\d+";
//    public static final String REAL_NUMBER = INTEGER + DECIMAL_POINT + INTEGER;
//    public static final String PERCENTAGE = REAL_NUMBER + "\\s?%";
//    public static final String COUNTER = "(" + INTEGER +")";
//    public static final String TIME = REAL_NUMBER + "s";
//    public static final String LOG_LEVEL= "(debug|info|warning|fine|finest)";
//    public static final String TAG = "(\\w+),?";

    /*
[0.165s][info ][gc,start     ] GC(1) Pause Young (Allocation Failure)
[0.165s][info ][gc,task      ] GC(1) Using 8 workers of 8 for evacuation
[0.170s][debug][gc,age       ] GC(1) Desired survivor size 1114112 bytes, new threshold 1 (max threshold 6)
[0.170s][info ][gc,heap      ] GC(1) ParNew: 19356K->1696K(19648K)
[0.170s][info ][gc,heap      ] GC(1) CMS: 130K->1179K(43712K)
[0.170s][info ][gc,metaspace ] GC(1) Metaspace: 5162K->5162K(1056768K)
[0.170s][info ][gc           ] GC(1) Pause Young (Allocation Failure) 19M->2M(61M) 5.221ms
[0.170s][info ][gc,cpu       ] GC(1) User=0.02s Sys=0.00s Real=0.00s
     */

    UnifiedGCLogTrace PARNEW_START = new UnifiedGCLogTrace();


}
