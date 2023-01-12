// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

/**
 * Contains the GCToolKit GC log parser. The parser is an internal module.
 */
module com.microsoft.gctoolkit.parser {
    requires com.microsoft.gctoolkit.api;
    requires java.logging;

    exports com.microsoft.gctoolkit.parser to
            com.microsoft.gctoolkit.api;

    exports com.microsoft.gctoolkit.parser.io to
            com.microsoft.gctoolkit.api;

    exports com.microsoft.gctoolkit.parser.jvm to
            com.microsoft.gctoolkit.api;

    exports com.microsoft.gctoolkit.parser.unified to
            com.microsoft.gctoolkit.api;

    exports com.microsoft.gctoolkit.parser.vmops to
            com.microsoft.gctoolkit.api;

    provides com.microsoft.gctoolkit.jvm.Diarizer with
            com.microsoft.gctoolkit.parser.jvm.PreUnifiedDiarizer,
            com.microsoft.gctoolkit.parser.jvm.UnifiedDiarizer;

    provides com.microsoft.gctoolkit.message.DataSourceParser with
            com.microsoft.gctoolkit.parser.JVMEventParser,
            com.microsoft.gctoolkit.parser.UnifiedJVMEventParser,
            com.microsoft.gctoolkit.parser.vmops.SafepointParser,
            com.microsoft.gctoolkit.parser.SurvivorMemoryPoolParser,
            com.microsoft.gctoolkit.parser.UnifiedSurvivorMemoryPoolParser,
            com.microsoft.gctoolkit.parser.CMSTenuredPoolParser,
            com.microsoft.gctoolkit.parser.GenerationalHeapParser,
            com.microsoft.gctoolkit.parser.UnifiedGenerationalParser,
            com.microsoft.gctoolkit.parser.PreUnifiedG1GCParser,
            com.microsoft.gctoolkit.parser.UnifiedG1GCParser,
            com.microsoft.gctoolkit.parser.ShenandoahParser,
            com.microsoft.gctoolkit.parser.ZGCParser;
}