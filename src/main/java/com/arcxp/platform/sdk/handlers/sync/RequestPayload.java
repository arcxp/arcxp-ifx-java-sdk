package com.arcxp.platform.sdk.handlers.sync;

import com.arcxp.platform.sdk.handlers.Payload;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Parsed Payload specific for the input from custom endpoint and intercepter handlers.
 */
public final class RequestPayload extends Payload {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RequestError error;

    @JsonInclude(JsonInclude.Include.NON_NULL)

    private String uri;


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
}
