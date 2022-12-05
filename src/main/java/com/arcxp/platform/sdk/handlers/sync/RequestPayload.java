package com.arcxp.platform.sdk.handlers.sync;

import com.arcxp.platform.sdk.handlers.Payload;

/**
 * Parsed Payload specific for the input from custom endpoint and intercepter handlers.
 */
public final class RequestPayload extends Payload {
    private String uuid;
    private RequestError error;

    private String uri;
    private String currentUserId;

    /**
     * Randomly Generated UUID associated with the request from the calling application.
     *
     * @return The request UUID
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * An error that is generated and thrown during the request to be sent back to the calling application.
     *
     * @return The request error
     */
    public RequestError getError() {
        return error;
    }

    public void setError(RequestError error) {
        this.error = error;
    }

    /**
     * The URI used to generate this request from the calling application.
     *
     * @return URI String
     */
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * The User that generated the request on the calling application.
     *
     * @return The user id on the calling application
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
}
