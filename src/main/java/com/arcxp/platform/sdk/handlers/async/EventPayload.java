package com.arcxp.platform.sdk.handlers.async;

import java.util.Date;

import com.arcxp.platform.sdk.handlers.Payload;

/**
 * Parsed Payload specific for event handlers.
 */

public final class EventPayload extends Payload {
    private Date time;


    public Date getTime() {
        return time;
    }


    public void setTime(Date time) {
        this.time = time;
    }
}
