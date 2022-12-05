package com.arcxp.platform.sdk.handlers.sync;

/**
 * Throwable exceptions for use inside Synchronous request handlers like custom endpoints and intercepters.
 */
public final class RequestException extends RuntimeException {
    private final String code;
    private final String message;

    public RequestException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public RequestException(String error) {
        String[] vals = error.split(":");
        this.code = vals[0];
        this.message = vals[1];
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
