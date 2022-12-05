package com.arcxp.platform.sdk.handlers.sync;

/**
 * Error Response for Synchronous requests like custom endpoint and intercepter exceptions.
 */
public class RequestError {
    private final String code;
    private final String message;

    /**
     * Constructor for Request Error messages.
     *
     * @param code    The error code
     * @param message The error message
     */
    public RequestError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
