package com.arcxp.platform.sdk.http;

/**
 * Wrapper class for Multi Part Form Data to be sent with HTTP Clients.
 */
public class MultiPartFormData {
    private final String name;
    private final Object body;
    private final ContentType contentType;

    /**
     * Constructor for multi part form data posts.
     *
     * @param name        The name of this part of the request body
     * @param body        The body data
     * @param contentType The content type of the body
     */
    public MultiPartFormData(String name, Object body, ContentType contentType) {
        this.name = name;
        this.body = body;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public Object getBody() {
        return body;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public enum ContentType {
        APPLICATION_JSON
    }

}
