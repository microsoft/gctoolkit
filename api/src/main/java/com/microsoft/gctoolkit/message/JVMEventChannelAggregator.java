package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;

public class JVMEventChannelAggregator implements JVMEventChannelListener {

    private ChannelName channel;
    private Aggregator aggregator;

    public JVMEventChannelAggregator(ChannelName channel, Aggregator aggregator) {
        this.channel = channel;
        this.aggregator = aggregator;
    }

    @Override
    public ChannelName channel() {
        return channel;
    }

    @Override
    public void receive(JVMEvent payload) {
        aggregator.receive(payload);
    }
}
