package com.arcxp.platform.sdk.handlers.sync;

import com.arcxp.platform.sdk.handlers.Payload;

/**
 * Parsed Payload specific for the input from custom endpoint and intercepter handlers.
 */
public final class RequestPayload extends Payload {
    private RequestError error;

    private String uri;

    private String currentUserId;

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
    @Deprecated
    public String getUri() {
        return uri;
    }

    @Deprecated
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
