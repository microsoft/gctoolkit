package com.microsoft.gctoolkit.integration;

import com.microsoft.gctoolkit.integration.aggregation.RuntimeAggregation;
import com.microsoft.gctoolkit.time.DateTimeStamp;
import org.junit.jupiter.api.Test;

public class RuntimeAggregationTest {

    @Test
    public void assertRecordAcceptsNull() {
        final double duration = 1.5d;
        RuntimeAggregation testRuntimeAggregation = new RuntimeAggregation();
        DateTimeStamp first = new DateTimeStamp(0.0);
        testRuntimeAggregation.publish(null, duration);
        DateTimeStamp last = first.add(duration);
        testRuntimeAggregation.publish(null, duration);
        last = last.add(duration);
        //Assertions.assertEquals(first, testRuntimeAggregation.getTimeOfFirstEvent());
        //Assertions.assertEquals(last, testRuntimeAggregation.getTimeOfLastEvent());
        // expect runtime duration to be the time of last event in this case
        // because timeOfLastEvent().minus(timeOfFirstEvent()) does not exceed
        // com.li.censum.aggregations.RuntimeAggregation.LOG_FRAGMENT_THRESHOLD.
//        double runtimeDuration = testRuntimeAggregation.getTimeOfLastEvent().getTimeStamp();
//        Assertions.assertEquals(runtimeDuration, testRuntimeAggregation.getRuntimeDuration());

        final int nTimes = 10;
        for (int n = 0; n < nTimes; n++) {
            testRuntimeAggregation.publish(null, duration);
        }
        // expect runtime duration to be timeOfLastEvent().minus(timeOfFirstEvent())
        // in this case since the difference exceeds
        // com.li.censum.aggregations.RuntimeAggregation.LOG_FRAGMENT_THRESHOLD.
//        runtimeDuration =
//                testRuntimeAggregation.getTimeOfLastEvent().minus(testRuntimeAggregation.getTimeOfFirstEvent());
//        Assertions.assertEquals(runtimeDuration, testRuntimeAggregation.getRuntimeDuration());
    }

    @Test
    public void assertRecordAcceptsNaN() {
        final double duration = Double.NaN;
        final double deltaTime = 1.0d;
        RuntimeAggregation testRuntimeAggregation = new RuntimeAggregation();
        DateTimeStamp first = new DateTimeStamp(0.0d);
        DateTimeStamp last = first.add(deltaTime);
        testRuntimeAggregation.publish(first, Double.NaN);
        testRuntimeAggregation.publish(last, Double.NaN);
//        Assertions.assertEquals(first, testRuntimeAggregation.getTimeOfFirstEvent());
//        Assertions.assertEquals(last, testRuntimeAggregation.getTimeOfLastEvent());
        // expect runtime duration to be the time of last event in this case
        // because timeOfLastEvent().minus(timeOfFirstEvent()) does not exceed
        // com.li.censum.aggregations.RuntimeAggregation.LOG_FRAGMENT_THRESHOLD.
//        double runtimeDuration = testRuntimeAggregation.getTimeOfLastEvent().getTimeStamp();
//        Assertions.assertEquals(runtimeDuration, testRuntimeAggregation.getRuntimeDuration());

        final int nTimes = 10;
        for (int n = 0; n < nTimes; n++) {
            last = last.add(deltaTime);
            testRuntimeAggregation.publish(last, duration);
        }
        // expect runtime duration to be timeOfLastEvent().minus(timeOfFirstEvent())
        // in this case since the difference exceeds
        // com.li.censum.aggregations.RuntimeAggregation.LOG_FRAGMENT_THRESHOLD.
//        runtimeDuration =
//                testRuntimeAggregation.getTimeOfLastEvent().minus(testRuntimeAggregation.getTimeOfFirstEvent());
//        Assertions.assertEquals(runtimeDuration, testRuntimeAggregation.getRuntimeDuration());
    }

    @Test
    public void getTimeOfFirstEvent() {
        final double duration = 1.5d;
        final double deltaTime = 1.0d;
        RuntimeAggregation testRuntimeAggregation = new RuntimeAggregation();
        DateTimeStamp first = new DateTimeStamp(0.0);
        DateTimeStamp last = first;
        final int nTimes = 10;
        for (int n = 0; n < nTimes; n++) {
            testRuntimeAggregation.publish(last, duration);
            last = last.add(deltaTime);
        }
//        Assertions.assertEquals(first, testRuntimeAggregation.getTimeOfFirstEvent());
    }

    @Test
    public void getTimeOfLastEvent() {
        final double duration = 1.5d;
        final double deltaTime = 1.0d;
        RuntimeAggregation testRuntimeAggregation = new RuntimeAggregation();
        final DateTimeStamp first = new DateTimeStamp(0.0);
        DateTimeStamp next = first;
        final int nTimes = 10;
        for (int n = 0; n < nTimes; n++) {
            testRuntimeAggregation.publish(next, duration);
            next = next.add(deltaTime);
        }
//        DateTimeStamp last = first.add(testRuntimeAggregation.getRuntimeDuration());
//        Assertions.assertEquals(last, testRuntimeAggregation.getTimeOfLastEvent());
    }

    @Test
    public void getRuntimeDuration() {
        final double duration = 1.5d;
        final double deltaTime = 1.0d;
        RuntimeAggregation testRuntimeAggregation = new RuntimeAggregation();
        final DateTimeStamp first = new DateTimeStamp(0.0);
        DateTimeStamp next = first;
        final int nTimes = 10;
        for (int n = 0; n < nTimes; n++) {
            testRuntimeAggregation.publish(next, duration);
            next = next.add(deltaTime);
        }
//        double totalDuration = testRuntimeAggregation.getTimeOfLastEvent().minus(testRuntimeAggregation.getTimeOfFirstEvent());
//        Assertions.assertEquals(totalDuration, testRuntimeAggregation.getRuntimeDuration());
    }
}