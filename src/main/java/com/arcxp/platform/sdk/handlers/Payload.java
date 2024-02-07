package com.arcxp.platform.sdk.handlers;

import java.util.Date;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Payload object to be sent to an integration handler.
 */

public class Payload {
    private int version;
    private String key;
    private ObjectNode body;
    private int typeId;
    private Date time;
    private String uuid;

    private String currentUserId;

    /**
     * The version of the payload.
     *
     * @return The Version
     */
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * The Event/Request key that is the source of this payload.
     *
     * @return The Key
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Payload Body for this Event/Request.
     *
     * @return JSON Body of the payload
     */
    public ObjectNode getBody() {
        return body;
    }

    public void setBody(ObjectNode body) {
        this.body = body;
    }

    /**
     * The type of request.
     * Ex
     * 1 - Async Event
     * 2 - Request Intercepter
     * 3 - Response Intercepter
     * 4 - Custom Endpoint
     * 5 - Sync Event
     * @return The type
     */
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    /**
     * The event time.
     *
     * @return The Time
     */
    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Randomly Generated UUID associated with the event payload from the calling application.
     *
     * @return The UUID
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
}
