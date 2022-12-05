package com.arcxp.platform.sdk.broker;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Broker class that delagates Lambda Payload Requests to the appropriate handlers for each Handler type.
 */

@Component
public final class MessageBroker {

    private static final Logger LOG = LoggerFactory.getLogger(MessageBroker.class);
    private static final String DEFAULT_NAMESPACE = "commerce:";
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
        Payload payload = createPayload(message);

        if (payload.getTypeId() == 1 /*Event*/) {
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
                    rplKey = rpl.getUri();
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

    private Payload createPayload(String message) {
        Payload payload = null;
        ObjectNode node = null;

        try {
            node = (ObjectNode) objectMapper.readTree(message);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to deserialize", e);
        }

        if (node.has("eventType")) {
            payload = new EventPayload();
            payload.setKey(addNamespace(node.get("eventType").asText()));
            payload.setTypeId(1);
            ((EventPayload) payload).setTime(new Date(node.get("eventTime").asLong()));
        } else if (node.has("key")) {
            payload = new RequestPayload();
            RequestPayload rpl = (RequestPayload) payload;
            rpl.setUuid(node.get("uuid").asText());
            rpl.setKey(addNamespace(node.get("key").asText()));
            rpl.setTypeId(node.get("typeId").asInt());
            rpl.setUri(node.get("uri").asText());
            rpl.setCurrentUserId(node.get("currentUserId").asText());
        }

        payload.setBody((ObjectNode) node.get("body"));
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

    private Payload processErrors(Payload payload) {
        Payload out = payload;
        if (payload instanceof RequestPayload) {
            out = new RequestOutPayload();
            ((RequestOutPayload) out).setUuid(((RequestPayload) payload).getUuid());
            ((RequestOutPayload) out).setError(((RequestPayload) payload).getError());
        }

        out.setBody(payload.getBody());
        return out;
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

}
