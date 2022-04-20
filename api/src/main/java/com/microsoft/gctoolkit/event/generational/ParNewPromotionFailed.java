// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class ParNewPromotionFailed extends ParNew {

    public ParNewPromotionFailed(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, GarbageCollectionTypes.ParNewPromotionFailed, gcCause, pauseTime);
    }

    public ParNewPromotionFailed(DateTimeStamp dateTimeStamp, double pauseTime) {
        this(dateTimeStamp, GCCause.PROMOTION_FAILED, pauseTime);
    }
}
