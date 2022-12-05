package com.arcxp.platform.sdk.handlers;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Payload object to be sent to an integration handler.
 */

public class Payload {
    private String key;
    private ObjectNode body;
    private int typeId;

    /**
     * The Event/Request key that is the source of this payload.
     *
     * @return The Key
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Payload Body for this Event/Request.
     *
     * @return JSON Body of the payload
     */
    public ObjectNode getBody() {
        return body;
    }

    public void setBody(ObjectNode body) {
        this.body = body;
    }

    /**
     * The type of request.
     * Ex
     * 1 - Event
     * 2 - Request Intercepter
     * 3 - Response Intercepter
     * 4 - Custom Endpoint
     *
     * @return The type
     */
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
