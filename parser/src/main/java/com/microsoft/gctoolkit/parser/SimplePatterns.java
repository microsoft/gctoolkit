// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

public interface SimplePatterns extends PreUnifiedTokens {

    /*
     0.908: [GC 75214K(118604K), 0.0010423 secs]
     0.933: [GC 91958K->92012K(118604K), 0.0493966 secs]
     1.028: [Full GC 109036K->44313K(118604K), 0.1666489 secs]
     */
    //285.945: [ParNew 173250K->163849K(190460K), 0.0044482 secs]
    GCParseRule PARNEW_NO_DETAILS = new GCParseRule("PARNEW_NO_DETAILS", DATE_TIMESTAMP + "\\[ParNew " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //81627.388: [ParNew
    //17552K->6881K(79808K), 0.0025012 secs]
    GCParseRule PARNEW_START = new GCParseRule("PARNEW_START", "^" + DATE_TIMESTAMP + "\\[ParNew$");

    //1745909.793: [GC
    GCParseRule GC_START = new GCParseRule("GC_START", GC_PREFIX + "$");
    GCParseRule YOUNG_SPLIT_NO_DETAILS = new GCParseRule("YOUNG_SPLIT_NO_DETAILS", "^" + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");
    GCParseRule GC_PREFIX_RULE = new GCParseRule("GC_PREFIX_RULE", GC_PREFIX);

    //234220.836: [GC 6277788K->3871140K(8212544K), 0.1040290 secs]
    GCParseRule YOUNG_NO_DETAILS = new GCParseRule("YOUNG_NO_DETAILS", DATE_TIMESTAMP + "\\[GC " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //CMS pause phase
    GCParseRule CMS_NO_DETAILS = new GCParseRule("CMS_NO_DETAILS", DATE_TIMESTAMP + "\\[GC " + OCCUPANCY_CONFIGURED_PAUSE);

    //29.975: [Full GC 155252K->44872K(205568K), 0.3398460 secs]
    GCParseRule FULL_NO_GC_DETAILS = new GCParseRule("FULL_NO_GC_DETAILS", DATE_TIMESTAMP + "\\[Full GC " + BEFORE_AFTER_CONFIGURED_PAUSE);

    //134883.104: [GC-- 2411642K->2456159K(2500288K), 0.1478340 secs]
    GCParseRule CMF_SIMPLE = new GCParseRule("CMF_SIMPLE", DATE_TIMESTAMP + "\\[GC-- " + BEFORE_AFTER_CONFIGURED_PAUSE + "\\]");

}
