// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

/*
   KCP - do not delete --- this is a deprecated GC combination. This interface is current not used but is retrained here for historical reasons.
 */
public interface ParNewSerial extends PreUnifiedTokens {

    //12.008: [GC 12.008: [ParNew: 4680K->224K(4992K), 0.0009052 secs]12.009: [Tenured: 11421K->7456K(11448K), 0.0679043 secs] 15860K->7456K(16440K), [Perm : 10677K->10677K(21248K)], 0.0689327 secs] [Times: user=0.07 sys=0.00, real=0.07 secs]
//    public static final Pattern PARNEW_SERIAL_FULL = Pattern.compile( DATE_TIMESTAMP + "\\[GC " + PARNEW_BLOCK + TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);

    //: 7077K->86K(7552K), 0.0019757 secs]20.774: [Tenured: 17007K->7966K(17080K), 0.0754072 secs] 23865K->7966K(24632K), [Perm : 10697K->10697K(21248K)], 0.0776402 secs]
    //]20.774: [Tenured: 17007K->7966K(17080K), 0.0754072 secs] 23865K->7966K(24632K), [Perm : 10697K->10697K(21248K)], 0.0776402 secs]
//    public static final Pattern PARNEW_SERIAL_FULL_SPLIT = Pattern.compile( "^: " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]" + TENURED_BLOCK + " " + BEFORE_AFTER_CONFIGURED + ", " + PERM_RECORD + ", " + PAUSE_TIME);

/*

    public static final Pattern PAR_OLD_REDUCTION = Pattern.compile("\\[ParOldGen: " + FROM_TO_CONFIGURED + "\\]");
    
    public static final String  TENURED_AND_PERM_REDUCTION =    TIMESTAMP + ": \\[Tenured: " + FROM_TO_CONFIGURED_PAUSE + "\\] ?" + FROM_TO_CONFIGURED + ", " + PERM_REDUCTION    + ", " + PAUSE_TIME + "\\]";
    public static final String  TENURED_AND_NO_PERM_REDUCTION = TIMESTAMP + ": \\[Tenured: " + FROM_TO_CONFIGURED_PAUSE + "\\] ?" + FROM_TO_CONFIGURED + ", " + PERM_NO_REDUCTION + ", " + PAUSE_TIME + "\\]";

    //2.783: [Full GC 2.783: [Tenured: 2684K->3969K(8192K), 0.0569037 secs] 4603K->3969K(13696K), [Perm : 16382K->16382K(16384K)], 0.0569750 secs]
    public static final Pattern PARALLEL_OLD_PERM_REDUCTION = Pattern.compile("\\[Full GC " + TIMESTAMP + ": \\[Tenured: " + FROM_TO_CONFIGURED + ", " + PAUSE_TIME + "\\] " + FROM_TO_CONFIGURED + ", " + PERM_REDUCTION.toString() + ", " + PAUSE_TIME + "\\]");
    public static final Pattern PARALLEL_OLD_NO_PERM_REDUCTION = Pattern.compile("\\[Full GC " + TIMESTAMP + ": \\[Tenured: " + FROM_TO_CONFIGURED + ", " + PAUSE_TIME + "\\] " + FROM_TO_CONFIGURED + ", " + PERM_NO_REDUCTION.toString() + ", " + PAUSE_TIME + "\\]");

    //TODO
    //0.963: [ParNew: 16256K->1984K(18240K), 0.0689940 secs] 16256K->6122K(58752K), 0.0691170 secs]
    public static final Pattern PARNEW = Pattern.compile( "\\[ParNew: " + FROM_TO_CONFIGURED_PAUSE + "\\] " + FROM_TO_CONFIGURED_PAUSE + "\\]");
    public static final Pattern PARNEW_ICMS = Pattern.compile("\\[ParNew: " + FROM_TO_CONFIGURED_PAUSE + "\\] " + FROM_TO_CONFIGURED + CMSPatterns.ICMS_DC + ", " + PAUSE_TIME + "\\]");

    //25.118: [GC 25.118: [ParNew: 1258368K->143985K(1415616K), 0.0987560 secs] 1265723K->151341K(8231360K), 0.0989880 secs] [Times: user=0.96 sys=0.49, real=0.09 secs]
    public static final Pattern PARNEW_COMPLETE = Pattern.compile( START_OF_GC_TIMESTAMP + "\\[GC\\s*" + GC_TIMESTAMP + PARNEW);

    //25.030: [GC 25.030: [ParNew: 451942K->51072K(460096K), 1.7048460 secs] 451942K->115934K(12531840K) icms_dc=5 , 1.7050420 secs]
    public static final Pattern PARNEW_COMPLETE_ICMS = Pattern.compile( START_OF_GC_TIMESTAMP + "\\[GC\\s*" + GC_TIMESTAMP + PARNEW_ICMS);
    public static final Pattern PARNEW_PRINT_HEAP_AT_GC = Pattern.compile( START_OF_GC_TIMESTAMP + "\\[ParNew: " + FROM_TO_CONFIGURED_PAUSE + "\\]" + FROM_TO_CONFIGURED + "Heap after GC invocations=");
    public static final Pattern PARNEW_PRINT_HEAP_AT_GC_PAUSE_TIME = Pattern.compile( "} , " + TIMESTAMP + "]");


    //939.183: [GC [PSYoungGen: 523744K->844K(547584K)] 657668K->135357K(1035008K), 0.0157986 secs] [Times: user=0.30 sys=0.01, real=0.02 secs]
    public static final Pattern PSYOUNGGEN_COMPLETE = Pattern.compile("\\[GC \\[PSYoungGen: " + FROM_TO_CONFIGURED + "\\] " + FROM_TO_CONFIGURED_PAUSE + "\\]");

    //Tenuring records
    //Desired survivor size 107347968 bytes, new threshold 1 (max 4)
    public static final Pattern TENURING_SUMMARY = Pattern.compile("Desired survivor size (\\d+) bytes, new threshold (\\d+) \\(max (\\d+)\\)");
    public static final Pattern TENURING_AGE_BREAKDOWN = Pattern.compile("- age\\s+(\\d+):\\s+(\\d+) bytes,\\s+(\\d+) total");

    public static final Pattern YG_WITH_TENURING = Pattern.compile("\\[GC$");
    public static final Pattern PSYOUNGGEN_DETAILS = Pattern.compile("\\[PSYoungGen: " + FROM_TO_CONFIGURED + "\\] " +  FROM_TO_CONFIGURED_PAUSE + "\\]");
    public static final Pattern PS_FULL_GC_DETAILS = Pattern.compile("\\[Full GC \\[PSYoungGen:");

    public static final Pattern PS_FULL_WITH_UNLOADING = Pattern.compile("\\[Full GC\\[Unloading");
    public static final Pattern PS_FULL_WITH_UNLOADING_SYSTEM = Pattern.compile("\\[Full GC \\(System\\)\\[Unloading");

    //0.465: [GC 0.465: [DefNew: 17217K->17217K(19328K), 0.0000096 secs]0.465: [Tenured: 36555K->11838K(43008K), 0.0266298 secs] 53772K->11838K(62336K), [Perm : 2819K->2819K(21248K)], 0.0775828 secs]
    public static final Pattern DEFNEW_TENURED = Pattern.compile("\\[GC\\s*" + GC_TIMESTAMP + "\\[DefNew: .+ \\[Tenured:");

    //: 4806K->1427K(5504K), 0.0089964 secs]4.690: [Tenured: 8890K->8960K(8960K), 0.0683118 secs] 12365K->10317K(14464K), [Perm : 18543K->18543K(18688K)], 0.0775077 secs]
    public static final Pattern DEFNEW_TRIGGERED_FULL = Pattern.compile( "^: " + FROM_TO_CONFIGURED + ", .+ secs\\]" + TIMESTAMP + ": \\[Tenured:");
    public static final Pattern DEFNEW_TRIGGERED_FULL_PERMSWEEP = Pattern.compile( "^: " + FROM_TO_CONFIGURED + ", .+ secs\\]" + TENURED_AND_PERM_REDUCTION);
    public static final Pattern DEFNEW_TRIGGERED_FULL_PERMNOSWEEP = Pattern.compile( "^: " + FROM_TO_CONFIGURED + ", .+ secs\\]" + TENURED_AND_NO_PERM_REDUCTION);

    // 83.940: [Full GC 83.940: [Tenured: 75938K->76927K(126568K), 1.8452680 secs] 106725K->76927K(183592K), [Perm : 62463K->62463K(62464K)], 1.8454850 secs] [Times: user=1.84 sys=0.01, real=1.85 secs] 
    public static final Pattern SERIAL_FULL_SYSTEM = Pattern.compile("\\[Full GC \\(System\\) " + TENURED_AND_PERM_REDUCTION);
    
    //104401.329: [Full GC (System) [PSYoungGen: 0K->0K(524224K)] [PSFull: 4928117K->4928117K(20447232K)] 4928117K->4928117K(20971456K) [PSPermGen: 113238K->113238K(262144K)], 1.7623490 secs]
    public static final Pattern PS_FULL_SYSTEM = Pattern.compile("\\[Full GC \\(System\\) \\[PSYoungGen:");

    public static final Pattern PS_FULL = Pattern.compile("\\[Full GC \\[PSYoungGen:");
    
    //12525.344: [Full GC 12525.344: [ParNew
    public static final Pattern PARNEW_FULL_FOLLOWED_BY_DETAILS = Pattern.compile("\\[Full GC " + TIMESTAMP + ": \\[ParNew$");
    

    // 93733.352: [Full GC[Unloading class sun.reflect.GeneratedMethodAccessor3]
    // [PSYoungGen: 892029K->0K(1034432K)] [ParOldGen: 7324875K->5037686K(7340032K)] 8216904K->5037686K(8374464K) [PSPermGen: 54404K->54363K(122496K)], 10.6791310 secs] [Times: user=67.94 sys=0.11, real=10.68 secs] 
    public static final Pattern PAR_FULL_GC_WITH_CLASS_UNLOADING = Pattern.compile(PAR_OLD_REDUCTION + " " + FROM_TO_CONFIGURED + ".* " + PAUSE_TIME);

    //[PSPermGen: 7034K->7034K(21248K)]
    public static final Pattern PS_PERM_GEN = Pattern.compile( "\\[PSPermGen: [0-9]+K->[0-9]+K\\([0-9]+K\\)\\]");
    
    //: 4806K->1427K(5504K), 0.0089964 secs]4.690: [Tenured: 8890K->8960K(8960K), 0.0683118 secs] 12365K->10317K(14464K), [Perm : 18543K->18543K(18688K)], 0.0775077 secs]
    public static final Pattern TENURED_DETAILS_PERM_REDUCTION = Pattern.compile(": " + FROM_TO_CONFIGURED_PAUSE + "\\]" + TENURED_AND_PERM_REDUCTION);
    public static final Pattern TENURED_DETAILS_NO_PERM_REDUCTION = Pattern.compile( ": " + FROM_TO_CONFIGURED_PAUSE + "\\]" + TENURED_AND_NO_PERM_REDUCTION);

*/

}
