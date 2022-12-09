package com.microsoft.gctoolkit.message;

import com.microsoft.gctoolkit.aggregator.Aggregator;
import com.microsoft.gctoolkit.event.jvm.JVMEvent;

public class JVMEventChannelAggregator implements JVMEventChannelListener {

    private Channels channel;
    private Aggregator aggregator;

    public JVMEventChannelAggregator(Channels channel, Aggregator aggregator) {
        this.channel = channel;
        this.aggregator = aggregator;
    }

    @Override
    public Channels channel() {
        return channel;
    }

    @Override
    public void receive(JVMEvent payload) {
        aggregator.receive(payload);
    }
}
