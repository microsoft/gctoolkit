// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class ParNewPromotionFailed extends ParNew {

    public ParNewPromotionFailed(DateTimeStamp dateTimeStamp, GCCause gcCause, double pauseTime) {
        super(dateTimeStamp, GarbageCollectionTypes.ParNewPromotionFailed, gcCause, pauseTime);
    }

    public ParNewPromotionFailed(DateTimeStamp dateTimeStamp, double pauseTime) {
        this(dateTimeStamp, GCCause.PROMOTION_FAILED, pauseTime);
    }

    @Override
    public boolean isYoung() {
        return true;
    }

}
