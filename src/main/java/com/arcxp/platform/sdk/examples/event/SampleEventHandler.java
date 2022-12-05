package com.arcxp.platform.sdk.examples.event;

import com.arcxp.platform.sdk.annotations.ArcEvent;
import com.arcxp.platform.sdk.handlers.async.EventHandler;
import com.arcxp.platform.sdk.handlers.async.EventPayload;

/**
 * A sample event handler based on event key TEST_EVENT.  Prints event key and someVal field to log output.
 */
@ArcEvent({"TEST_EVENT"})
public class SampleEventHandler extends EventHandler {

    @Override
    public void handle(EventPayload eventPayload) {
        System.out
            .println("Received event " + eventPayload.getKey() + " " + eventPayload.getBody().get("someVal").asText());
    }
}
