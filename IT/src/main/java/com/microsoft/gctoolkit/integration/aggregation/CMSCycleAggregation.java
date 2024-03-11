package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregation;
import com.microsoft.gctoolkit.aggregator.Collates;

@Collates(CMSCycleAggregator.class)
public class CMSCycleAggregation extends Aggregation {

    private int initialMark = 0;
    private int remark = 0;
    private int concurrentEvent = 0;

    public void initialMark() {
        initialMark++;
    }

    public void remark() {
        remark++;
    }

    public void concurrentEvent() {
        concurrentEvent++;
    }

    public int getInitialMark() { return initialMark; }
    public int getRemark() { return remark; }
    public int getConcurrentEvent() { return concurrentEvent; }

    @Override
    public boolean hasWarning() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
