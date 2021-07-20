// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.censum.event.generational;

import com.microsoft.censum.event.GCCause;
import com.microsoft.censum.event.GarbageCollectionTypes;
import com.microsoft.censum.time.DateTimeStamp;

public class CMSRemark extends CMSPauseEvent {

    public CMSRemark(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GCCause.CMS_FINAL_REMARK, duration);
    }

    public CMSRemark(DateTimeStamp timeStamp, GCCause gcCause, double duration) {
        super(timeStamp, GarbageCollectionTypes.Remark, gcCause, duration);
    }


}
