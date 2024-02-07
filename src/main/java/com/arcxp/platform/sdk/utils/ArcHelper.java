package com.arcxp.platform.sdk.utils;

/**
 * Arc Helper Class.
 */
public class ArcHelper {
    private static final String ARC_DOMAIN = "arcpublishing.com";

    private static final String ARC_API_PREFIX = "api.";

    /**
     * Utility for determining if a hostname is an arc api based on convention.
     *
     * @param host The host value of the http call.
     * @return True if the hostname is an arc api.
     */
    public static boolean isArcAPI(String host) {
        return host != null && host.startsWith(ARC_API_PREFIX) && host.endsWith(ARC_DOMAIN);
    }
}
