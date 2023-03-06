package com.arcxp.platform.sdk.examples.request;

import com.arcxp.platform.sdk.annotations.ArcSyncEvent;
import com.arcxp.platform.sdk.handlers.sync.RequestHandler;
import com.arcxp.platform.sdk.handlers.sync.RequestPayload;
import org.springframework.stereotype.Component;

@Component
@ArcSyncEvent({"commerce:TEST_REQUEST"})
public class SampleRequestHandler extends RequestHandler {
    @Override
    public void handle(RequestPayload payload) {
        System.out
                .println("Received request " + payload.getKey() + " " + payload.getBody().get("someVal").asText());
    }
}
