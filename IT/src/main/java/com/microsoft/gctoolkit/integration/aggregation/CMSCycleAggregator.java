package com.microsoft.gctoolkit.integration.aggregation;

import com.microsoft.gctoolkit.aggregator.Aggregates;
import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.aggregator.EventSource;
import com.microsoft.gctoolkit.event.generational.CMSConcurrentEvent;
import com.microsoft.gctoolkit.event.generational.CMSRemark;
import com.microsoft.gctoolkit.event.generational.InitialMark;


@Aggregates({EventSource.GENERATIONAL})
public class CMSCycleAggregator extends Aggregator<CMSCycleAggregation> {

    private InitialMark lastInitialMark = null;
    private CMSRemark lastRemark = null;
    public CMSCycleAggregator(CMSCycleAggregation results) {
        super(results);
        register(InitialMark.class, this::count);
        register(CMSRemark.class, this::count);
        register(CMSConcurrentEvent.class, this::count);
    }

    public void count(InitialMark event) {
        if ( event.equals(lastInitialMark)) return;
        lastInitialMark = event;
        aggregation().initialMark();
    }

    public void count(CMSRemark event) {
        if ( event.equals(lastRemark)) return;
        lastRemark = event;
        aggregation().remark();
    }

    public void count(CMSConcurrentEvent event) {
        aggregation().concurrentEvent();
    }
}
