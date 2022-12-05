package com.arcxp.platform.sdk.handlers.async;


/**
 * Abstract Event Handler for asynchronous event handling.
 */
public abstract class EventHandler {

    /**
     * Client implemented handle method for asynchronous events.
     *
     * @param eventPayload The asynchronous event payload
     */
    public abstract void handle(EventPayload eventPayload);
}
