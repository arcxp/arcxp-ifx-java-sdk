package com.arcxp.platform.sdk.broker;

/**
 * Throwable exception for invalid event payload.
 */
public final class EventPayloadException extends RuntimeException {
    public EventPayloadException() {
        super("Invalid event payload");
    }
}
