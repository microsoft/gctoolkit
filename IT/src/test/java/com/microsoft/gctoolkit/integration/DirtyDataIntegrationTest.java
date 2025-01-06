package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.GCToolKit;
import com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary;
import com.microsoft.gctoolkit.integration.io.TestLogFile;
import com.microsoft.gctoolkit.io.GCLogFile;
import com.microsoft.gctoolkit.io.SingleGCLogFile;
import com.microsoft.gctoolkit.parser.GenerationalHeapParser;
import com.microsoft.gctoolkit.vertx.VertxDataSourceChannel;
import com.microsoft.gctoolkit.vertx.VertxJVMEventChannel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("classPath")
public class DirtyDataIntegrationTest {

  /**
   * Test a GC log file that contains only the header but no actual events
   */
  @Test
  public void testNoEventLog() {
    Path path = new TestLogFile("streaming/gc_no_event.log").getFile().toPath();
    assertThrows(IllegalStateException.class, () -> analyze(path.toString()));
  }

  @Test
  public void testEmptyFile() {
    Path path = new TestLogFile("streaming/gc_empty.log").getFile().toPath();
    assertThrows(IllegalStateException.class, () -> analyze(path.toString()));
  }

  public void analyze(String gcLogFile) throws IOException {
    GCLogFile logFile = new SingleGCLogFile(Path.of(gcLogFile));
    GCToolKit gcToolKit = new GCToolKit();

    gcToolKit.loadDataSourceChannel(new VertxDataSourceChannel());
    gcToolKit.loadJVMEventChannel(new VertxJVMEventChannel());
    gcToolKit.loadDataSourceParser(new GenerationalHeapParser());

    gcToolKit.loadAggregation(new CollectionCycleCountsSummary());

    gcToolKit.analyze(logFile);
  }
}
