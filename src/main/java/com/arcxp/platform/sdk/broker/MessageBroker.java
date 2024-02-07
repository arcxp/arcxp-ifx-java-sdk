package com.arcxp.platform.sdk.broker;

import com.arcxp.platform.sdk.annotations.ArcAsyncEvent;
import com.arcxp.platform.sdk.annotations.ArcEndpoint;
import com.arcxp.platform.sdk.annotations.ArcEvent;
import com.arcxp.platform.sdk.annotations.ArcRequestIntercept;
import com.arcxp.platform.sdk.annotations.ArcResponseIntercept;
import com.arcxp.platform.sdk.annotations.ArcSyncEvent;
import com.arcxp.platform.sdk.handlers.Payload;
import com.arcxp.platform.sdk.handlers.async.EventHandler;
import com.arcxp.platform.sdk.handlers.async.EventPayload;
import com.arcxp.platform.sdk.handlers.sync.RequestHandler;
import com.arcxp.platform.sdk.handlers.sync.RequestOutPayload;
import com.arcxp.platform.sdk.handlers.sync.RequestPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Broker class that delagates Lambda Payload Requests to the appropriate handlers for each Handler type.
 */

@Component
public final class MessageBroker {

    private static final Logger LOG = LoggerFactory.getLogger(MessageBroker.class);
    private static final String DEFAULT_NAMESPACE = "commerce:";

    private boolean shouldTransformPayload = true;
    private boolean shouldPropogateExceptions = false;

    @Lazy
    @Autowired
    private List<EventHandler> eventHandlers;
    @Lazy
    @Autowired
    private List<RequestHandler> requestHandlers;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Broker handler method for handling both async and sync requests.
     *
     * @param message Raw Lambda Message Payload
     * @return Handled Response
     */
    public String handle(String message) {
        Payload payload;

        try {
            payload = createPayload(message);
        } catch (Exception exception) {
            LOG.error(exception.getMessage(), exception);
            if (shouldPropogateExceptions) {
                throw new EventPayloadException(exception.getMessage());
            } else {
                throw new EventPayloadException();
            }
        }

        if (payload.getTypeId() == 1 /*Async Event*/) {
            processPayload(payload);
        } else {
            processPayloadWithReturn(payload);
        }
        Payload response = processErrors(payload);

        return stringifyResponse(response);
    }

    private void processPayload(Payload payload) {
        if (payload instanceof EventPayload && this.eventHandlers != null) {
            for (EventHandler currentHandler : this.eventHandlers) {
                ArcAsyncEvent annotation = currentHandler.getClass().getAnnotation(ArcAsyncEvent.class);
                String[] vals = new String[0];
                if (annotation == null) {
                    ArcEvent legacyAnnotation = currentHandler.getClass().getAnnotation(ArcEvent.class);
                    if (legacyAnnotation != null) {
                        vals = legacyAnnotation.value();
                    }
                } else {
                    vals = annotation.value();
                }
                for (String key : vals) {
                    if (payload.getKey().equalsIgnoreCase(key)) {
                        currentHandler.handle((EventPayload) payload);
                    }
                }
            }
        }

    }

    private void processPayloadWithReturn(Payload payload) {
        if (payload instanceof RequestPayload && this.requestHandlers != null) {
            RequestPayload rpl = (RequestPayload) payload;
            for (RequestHandler requestHandler : this.requestHandlers) {
                String[] vals = new String[0];
                String rplKey = rpl.getKey();
                if (rpl.getTypeId() == 2) {
                    ArcRequestIntercept legacyAnnotation = requestHandler.getClass()
                            .getAnnotation(ArcRequestIntercept.class);
                    if (legacyAnnotation == null) {
                        ArcSyncEvent annotation = requestHandler.getClass().getAnnotation(ArcSyncEvent.class);
                        if (annotation != null) {
                            rplKey = rpl.getKey() + "_BEFORE";
                            vals = annotation.value();
                        }
                    } else {
                        vals = legacyAnnotation.value();
                    }
                } else if (rpl.getTypeId() == 3) {
                    ArcResponseIntercept legacyAnnotation = requestHandler.getClass()
                            .getAnnotation(ArcResponseIntercept.class);
                    if (legacyAnnotation == null) {
                        ArcSyncEvent annotation = requestHandler.getClass().getAnnotation(ArcSyncEvent.class);
                        if (annotation != null) {
                            rplKey = rpl.getKey() + "_AFTER";
                            vals = annotation.value();
                        }
                    } else {
                        vals = legacyAnnotation.value();
                    }
                } else if (rpl.getTypeId() == 4) {
                    ArcSyncEvent annotation = requestHandler.getClass().getAnnotation(ArcSyncEvent.class);
                    if (annotation == null) {
                        ArcEndpoint legacyAnnotation = requestHandler.getClass().getAnnotation(ArcEndpoint.class);
                        if (legacyAnnotation != null) {
                            vals = legacyAnnotation.value();
                        }
                    } else {
                        vals = annotation.value();
                    }
                    rplKey = addNamespace(rpl.getUri());
                } else if (rpl.getTypeId() == 5) {
                    ArcSyncEvent annotation = requestHandler.getClass().getAnnotation(ArcSyncEvent.class);
                    if (annotation == null) {
                        continue;
                    }
                    vals = annotation.value();
                }
                for (String key : vals) {
                    if (rplKey.equalsIgnoreCase(key)) {
                        LOG.info("Handler:" + requestHandler.getClass().getSimpleName() + " URI:" + rpl.getUri());
                        requestHandler.handleWithReturn(rpl);
                    }
                }
            }
        }

    }

    private Payload createPayload(String message) throws JsonProcessingException {
        Payload payload = null;
        ObjectNode requestNode;

        try {
            requestNode = (ObjectNode) objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to deserialize incoming message payload", e);
            throw e;
        }

        // We should not transform the payload, and instead pass json directly to handlers. Payload is routed to a
        // handler based on the `key` property.
        if (!shouldTransformPayload) {
            boolean async = !requestNode.has("typeId") || "1".equals(requestNode.get("typeId").asText());
            payload = getDefaultPayload(async);

            // Override with values from requestNode
            overridePayloadWithRequestNode(payload, requestNode);

            return payload;
        }

        if (requestNode.has("version") && requestNode.get("version").asInt() > 1) {
            int typeId = requestNode.get("typeId").asInt();
            if (typeId == 1 || typeId == 5) {
                if (typeId == 1) {
                    payload = new EventPayload();
                } else {
                    payload = new RequestPayload();
                }
                payload.setCurrentUserId(requestNode.get("currentUserId").asText());
                payload.setVersion(requestNode.get("version").asInt());
                payload.setKey(addNamespace(requestNode.get("eventName").asText()));
                payload.setTypeId(typeId);

                // optional fields
                if (requestNode.hasNonNull("eventTime")) {
                    payload.setTime(new Date(requestNode.get("eventTime").asLong()));
                }
                if (requestNode.hasNonNull("uuid")) {
                    payload.setUuid(requestNode.get("uuid").asText());
                }
            }
        } else {
            // Legacy structure
            if (requestNode.has("eventType")) {
                payload = new EventPayload();
                payload.setVersion(1);
                payload.setKey(addNamespace(requestNode.get("eventType").asText()));
                payload.setTypeId(1);
                ((EventPayload) payload).setTime(new Date(requestNode.get("eventTime").asLong()));
            } else if (requestNode.has("key")) {
                payload = new RequestPayload();
                payload.setVersion(1);
                RequestPayload rpl = (RequestPayload) payload;
                rpl.setUuid(requestNode.get("uuid").asText());
                rpl.setKey(addNamespace(requestNode.get("key").asText()));
                rpl.setTypeId(requestNode.get("typeId").asInt());
                rpl.setUri(requestNode.get("uri").asText());
                rpl.setCurrentUserId(requestNode.get("currentUserId").asText());
            }
        }

        payload.setBody((ObjectNode) requestNode.get("body"));
        return payload;
    }

    private String stringifyResponse(Payload out) {
        String result = null;
        try {
            result = objectMapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to serialize", e);
        }
        return result;
    }

    /**
     * Processes the given payload for errors. If the payload is an instance of {@link Payload}
     * and contains an error, a new {@link RequestOutPayload} is created with the error
     * information and other details copied from the input payload.
     *
     * @param payload The input payload which is to be checked for errors.
     * @return A new {@link RequestOutPayload} containing the error details if an error is present,
     *         or the original payload if no errors are found.
     */
    private Payload processErrors(Payload payload) {
        if (payload instanceof RequestPayload && ((RequestPayload) payload).getError() != null) {
            RequestOutPayload out = new RequestOutPayload();
            out.setUuid(payload.getUuid());
            out.setError(((RequestPayload) payload).getError());
            out.setCurrentUserId(payload.getCurrentUserId());
            out.setBody(payload.getBody());
            return out;
        }
        return payload;
    }

    private String addNamespace(String key) {
        if (!key.contains(":")) {
            key = DEFAULT_NAMESPACE + key;
        }
        return key;
    }

    public void setEventHandlers(List<EventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
    }

    public void setRequestHandlers(List<RequestHandler> requestHandlers) {
        this.requestHandlers = requestHandlers;
    }

    public static class Data {
        Map<String, Object> data;
    }

    public void setShouldTransformPayload(boolean shouldTransformPayload) {
        this.shouldTransformPayload = shouldTransformPayload;
    }

    public void setShouldPropogateExceptions(boolean shouldPropogateExceptions) {
        this.shouldPropogateExceptions = shouldPropogateExceptions;
    }

    /**
     * Overrides the properties of a given Payload object with the values present in an ObjectNode.
     * The method checks for the presence of each expected property within the ObjectNode. If a property
     * is found and is non-null, its value is used to update the corresponding property in the Payload object.
     * The method specifically handles the 'key' property and ensures it is not blank. If it is, an exception is thrown.
     * For the 'time' property, the value is expected to be in epoch seconds and is converted to milliseconds.
     * The 'body' property is an ObjectNode and is merged with the existing body of the Payload.
     *
     * @param payload     The Payload object to be updated.
     * @param requestNode The ObjectNode containing potential override values.
     * @throws EventPayloadException If the 'key' property is missing or blank, or if the 'time' property is not in a
     *                               valid epoch format.
     */
    private void overridePayloadWithRequestNode(Payload payload, ObjectNode requestNode) throws EventPayloadException {
        // Update key if it's present and not blank
        if (requestNode.hasNonNull("key") && !StringUtils.isBlank(requestNode.get("key").asText())) {
            payload.setKey(requestNode.get("key").asText());
        } else {
            throw new EventPayloadException("Key must be provided and cannot be blank");
        }

        // Update other fields if present
        if (requestNode.hasNonNull("version")) {
            payload.setVersion(requestNode.get("version").asInt());
        }

        if (requestNode.hasNonNull("typeId")) {
            payload.setTypeId(requestNode.get("typeId").asInt());
        }

        if (requestNode.hasNonNull("time")) {
            String timeStr = requestNode.get("time").asText();
            try {
                long time = Long.parseLong(timeStr);
                payload.setTime(new Date(time * 1000));
            } catch (NumberFormatException e) {
                throw new EventPayloadException("Time is not a valid epoch format");
            }
        }

        if (requestNode.hasNonNull("uuid")) {
            payload.setUuid(requestNode.get("uuid").asText());
        }

        if (requestNode.hasNonNull("currentUserId")) {
            payload.setCurrentUserId(requestNode.get("currentUserId").asText());
        }

        // If the body is a nested object, you should merge it
        if (requestNode.hasNonNull("body") && requestNode.get("body").isObject()) {
            payload.getBody().setAll((ObjectNode) requestNode.get("body"));
        }
    }

    private Payload getDefaultPayload(boolean async) {
        Payload payload;
        if (async) {
            payload = new EventPayload();
            payload.setTypeId(1);
        } else {
            payload = new RequestPayload();
            payload.setTypeId(5);
        }
        payload.setBody(objectMapper.createObjectNode());
        payload.setVersion(2);
        payload.setTime(new Date());
        payload.setUuid("");
        payload.setCurrentUserId("");
        return payload;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
