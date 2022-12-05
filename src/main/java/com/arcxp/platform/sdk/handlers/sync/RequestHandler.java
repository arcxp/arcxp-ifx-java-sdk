package com.arcxp.platform.sdk.handlers.sync;

import com.arcxp.platform.sdk.broker.MessageBroker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for Handling Synchronous Requests like custom endpoints and intercepters.
 */
public abstract class RequestHandler {
    Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    @Autowired
    private MessageBroker messageBroker;
    @Autowired
    private ObjectMapper objectMapper;

    public void setMessageBroker(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    /**
     * Internal handler method that sets payload return values on exceptions.
     *
     * @param payload The Synchronous Request Payload
     */
    public void handleWithReturn(RequestPayload payload) {
        try {
            handle(payload);
        } catch (RequestException e) {
            payload.setError(new RequestError(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            logger.error("", e);
            payload.setError(new RequestError("9999", e.getMessage()));
        }
    }

    /**
     * Client implemented handle method for synchronous behavior.
     *
     * @param payload The Synchronous Request Payload
     */
    public abstract void handle(RequestPayload payload);

}
