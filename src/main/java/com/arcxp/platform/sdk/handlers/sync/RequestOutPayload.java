package com.arcxp.platform.sdk.handlers.sync;

import com.arcxp.platform.sdk.handlers.Payload;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Parsed Payload specific for the output from custom endpoints and intercepter handlers.
 */
public class RequestOutPayload extends Payload {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uuid;
    private ObjectNode body;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RequestError error;

    /**
     * A Randomly Generated UUID associated with this request.
     *
     * @return The request UUID
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public ObjectNode getBody() {
        return body;
    }

    @Override
    public void setBody(ObjectNode body) {
        this.body = body;
    }

    public RequestError getError() {
        return error;
    }

    public void setError(RequestError error) {
        this.error = error;
    }
}
