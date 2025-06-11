package com.microsoft.gctoolkit.event.zgc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ZGCYoungCollection extends ZGCCollection {
    public ZGCYoungCollection(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, GCCause cause, double duration) {
        super(timeStamp, gcType, cause, duration);
    }

    public ZGCYoungCollection(DateTimeStamp timeStamp, double duration) {
        super(timeStamp, duration);
    }

    public ZGCYoungCollection(DateTimeStamp timeStamp, GCCause cause, double duration) {
        super(timeStamp, cause, duration);
    }

    public ZGCYoungCollection(DateTimeStamp timeStamp, GarbageCollectionTypes gcType, double duration) {
        super(timeStamp, gcType, duration);
    }
}
