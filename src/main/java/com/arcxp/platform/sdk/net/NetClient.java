package com.arcxp.platform.sdk.net;

/**
 * Debug websocket interface.
 */
public interface NetClient {

    /**
     * Sends debug message.
     *
     * @param message the debug response message
     */
    void send(String message);

    /**
     * Receive a dubug message.
     *
     * @param message the debug receive message
     */
    void receive(String message);
}
