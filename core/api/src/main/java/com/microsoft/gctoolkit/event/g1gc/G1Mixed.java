// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.g1gc;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;


public class G1Mixed extends G1Young {

    public G1Mixed(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, GarbageCollectionTypes.Mixed, gcCause, pauseTime);
    }

}
