package com.arcxp.platform.sdk.net.ws;

/**
 * Internal websocket utility for connecting to debug websocket.
 */
public class WebSocketUtil {

    /**
     * Default secure websocket for everything but localhost.
     *
     * @param host Websocket Hostname
     * @param app  Commerce App to connect to over websocket
     * @return The secure or insecure websocket uri
     */
    public static String buildWSUri(String host, String app) {

        String url = "://" + host + "/" + app + "/api/v1/integration/debug";
        if (host.contains("localhost")) {
            url = "ws" + url;
        } else {
            url = "wss" + url;
        }
        return url;
    }
}
