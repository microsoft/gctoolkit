// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.parser;

public interface TenuredPatterns extends PreUnifiedTokens {

    GCParseRule TENURING_SUMMARY = new GCParseRule("TENURING_SUMMARY", "Desired survivor size " + COUNTER + " bytes, new threshold " + COUNTER + " \\(max(?: threshold)? " + COUNTER + "\\)");
    GCParseRule TENURING_AGE_BREAKDOWN = new GCParseRule("TENURING_AGE_BREAKDOWN", "- age\\s+" + COUNTER + ":\\s+" + COUNTER + " bytes,\\s+" + COUNTER + " total");

}
