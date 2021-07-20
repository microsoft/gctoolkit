// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.g1gc;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;


public class G1Mixed extends G1Young {

    public G1Mixed(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, GarbageCollectionTypes.Mixed, gcCause, pauseTime);
    }

}
