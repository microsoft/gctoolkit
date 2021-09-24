// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.gctoolkit.event.generational;

import com.microsoft.gctoolkit.event.GCCause;
import com.microsoft.gctoolkit.event.GarbageCollectionTypes;
import com.microsoft.gctoolkit.time.DateTimeStamp;

public class CMSRemark extends CMSPauseEvent {

    public CMSRemark(DateTimeStamp timeStamp, double duration) {
        this(timeStamp, GCCause.CMS_FINAL_REMARK, duration);
    }

    public CMSRemark(DateTimeStamp timeStamp, GCCause gcCause, double duration) {
        super(timeStamp, GarbageCollectionTypes.Remark, gcCause, duration);
    }


}
