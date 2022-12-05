package com.arcxp.platform.sdk.handlers.async;

/**
 * Exception that occurs in an event handler.
 */

public class EventException extends RuntimeException {
    private final String code;
    private final String message;

    /**
     * Constructor for Event Exceptions.
     *
     * @param code    The error code
     * @param message The error message
     */
    public EventException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Short Hand Constructor for Event Exceptions.
     *
     * @param error The Error Code and Message split with a colon
     */
    public EventException(String error) {
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
