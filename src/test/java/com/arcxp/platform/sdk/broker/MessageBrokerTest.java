package com.arcxp.platform.sdk.broker;

import com.arcxp.platform.sdk.annotations.ArcAsyncEvent;
import com.arcxp.platform.sdk.annotations.ArcEndpoint;
import com.arcxp.platform.sdk.annotations.ArcRequestIntercept;
import com.arcxp.platform.sdk.annotations.ArcResponseIntercept;
import com.arcxp.platform.sdk.annotations.ArcSyncEvent;
import com.arcxp.platform.sdk.handlers.Payload;
import com.arcxp.platform.sdk.handlers.async.EventHandler;
import com.arcxp.platform.sdk.handlers.async.EventPayload;
import com.arcxp.platform.sdk.handlers.sync.RequestHandler;
import com.arcxp.platform.sdk.handlers.sync.RequestPayload;
import com.arcxp.platform.sdk.utils.MapUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MessageBrokerTest {

    @InjectMocks
    private MessageBroker messageBroker;

    private ObjectMapper objectMapper;

    private String calledHandlerName;

    private Payload calledPayload;

    @ArcRequestIntercept("commerce:CART_ADD")
    private class CartAddRequestInterceptor extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "CartAddRequestInterceptor";
        }
    }

    @ArcSyncEvent("commerce:SYNC_TEST_BEFORE")
    private class TestSyncBefore extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "TestSyncBefore";
            calledPayload = payload;
        }
    }

    @ArcResponseIntercept("commerce:CART_ADD")
    private class CartAddResponseInterceptor extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "CartAddResponseInterceptor";
        }
    }

    @ArcSyncEvent("commerce:SYNC_TEST_AFTER")
    private class TestSyncAfter extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "TestSyncAfter";
            calledPayload = payload;
        }
    }

    @ArcEndpoint("commerce:some/url/1")
    private class LegacySampleEndpoint extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "LegacySampleEndpoint";
        }
    }

    @ArcSyncEvent("commerce:some/url/2")
    private class SampleEndpoint extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "SampleEndpoint";
            calledPayload = payload;
        }
    }

    @ArcAsyncEvent("pagebuilder:VERIFY_EMAIL")
    private class TestAsync extends EventHandler {
        @Override
        public void handle(EventPayload payload) {
            calledHandlerName = "TestAsync";
            calledPayload = payload;
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.objectMapper = MapUtils.createObjectMapper();
        messageBroker.setObjectMapper(MapUtils.createObjectMapper());
        calledHandlerName = null;
        calledPayload = null;
        setupRequestHandlers();
        setupEventHandlers();
    }


    @Test
    public void testCartAddRequestInterceptor() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("key", "CART_ADD");
        requestPayloadNode.put("typeId", 2);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("uri", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("CartAddRequestInterceptor", calledHandlerName);
    }

    @Test
    public void testTestSyncBefore() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("key", "SYNC_TEST");
        requestPayloadNode.put("typeId", 2);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("uri", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("TestSyncBefore", calledHandlerName);
    }

    @Test(expected = EventPayloadException.class)
    public void testRequestInterceptorShouldThrowForV2_DueToMissingBody() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("eventName", "commerce:SYNC_TEST");
        requestPayloadNode.put("version", 2);
        requestPayloadNode.put("typeId", 2);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));
    }

    @Test
    public void testCartAddResponseInterceptor() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("key", "CART_ADD");
        requestPayloadNode.put("typeId", 3);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("uri", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("CartAddResponseInterceptor", calledHandlerName);
    }

    @Test
    public void testTestSyncAfter() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("key", "SYNC_TEST");
        requestPayloadNode.put("typeId", 3);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("uri", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("TestSyncAfter", calledHandlerName);
    }

    @Test(expected = EventPayloadException.class)
    public void testResponseInterceptorShouldThrowForV2_DueToMissingBody() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("eventName", "commerce:SYNC_TEST");
        requestPayloadNode.put("version", 2);
        requestPayloadNode.put("typeId", 3);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));
    }

    @Test
    public void testLegacySampleEndpoint() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("key", "");
        requestPayloadNode.put("typeId", 4);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("uri", "some/url/1");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("LegacySampleEndpoint", calledHandlerName);
    }

    @Test
    public void testSampleEndpoint() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("key", "");
        requestPayloadNode.put("typeId", 4);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("uri", "some/url/2");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("SampleEndpoint", calledHandlerName);
    }

    @Test(expected = EventPayloadException.class)
    public void testCustomEndpointShouldThrowForV2_DueToMissingBody() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("eventName", "commerce:some/url");
        requestPayloadNode.put("version", 2);
        requestPayloadNode.put("typeId", 4);
        requestPayloadNode.put("uuid", "");
        requestPayloadNode.put("currentUserId", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));
    }

    @Test
    public void testSyncEventV2() throws IOException {
        ObjectNode requestPayloadNode = objectMapper.createObjectNode();
        requestPayloadNode.put("eventName", "commerce:some/url/2");
        requestPayloadNode.put("version", 2);
        requestPayloadNode.put("typeId", 5);
        requestPayloadNode.put("uuid", "uuid123");
        requestPayloadNode.put("currentUserId", "userid123");

        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("test", "sync body");
        requestPayloadNode.set("body", bodyNode);

        this.messageBroker.handle(objectMapper.writeValueAsString(requestPayloadNode));

        assertEquals("SampleEndpoint", calledHandlerName);
        assertEquals(2, calledPayload.getVersion());
        assertEquals(5, calledPayload.getTypeId());
        assertEquals("uuid123", calledPayload.getUuid());
        assertEquals("userid123", ((RequestPayload) calledPayload).getCurrentUserId());
        assertEquals("sync body", calledPayload.getBody().get("test").asText());
    }

    @Test
    public void testAsyncEventWithNamespace() throws IOException {
        ObjectNode eventPayloadNode = objectMapper.createObjectNode();
        eventPayloadNode.put("eventType", "pagebuilder:VERIFY_EMAIL");
        eventPayloadNode.put("eventTime", "");

        this.messageBroker.handle(objectMapper.writeValueAsString(eventPayloadNode));

        assertEquals("TestAsync", calledHandlerName);
    }

    @Test
    public void testAsyncEventV2WithNamespace() throws IOException {
        // Create sample payload
        ObjectNode eventPayloadNode = objectMapper.createObjectNode();

        // Set up the properties directly on the ObjectNode
        eventPayloadNode.put("eventName", "pagebuilder:VERIFY_EMAIL");
        eventPayloadNode.put("version", 2);
        eventPayloadNode.put("typeId", 1);
        eventPayloadNode.put("eventTime", 1648496000);
        eventPayloadNode.put("currentUserId", "");

        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("test", "async body");
        eventPayloadNode.set("body", bodyNode);


        this.messageBroker.handle(objectMapper.writeValueAsString(eventPayloadNode));

        assertEquals("TestAsync", calledHandlerName);
        assertEquals(2, calledPayload.getVersion());
        assertEquals(1, calledPayload.getTypeId());
        assertEquals(1648496000, calledPayload.getTime().getTime());
        assertEquals("async body", calledPayload.getBody().get("test").asText());
    }

    @Test(expected = EventPayloadException.class)
    public void testPayloadPassedDirectlyToHandler_ShouldThrowIfKeyNotProvided() throws IOException {
        // Create sample payload
        ObjectNode eventPayloadNode = objectMapper.createObjectNode();

        // Set up the properties directly on the ObjectNode
        eventPayloadNode.put("eventName", "pagebuilder:VERIFY_EMAIL");
        eventPayloadNode.put("version", 2);
        eventPayloadNode.put("typeId", 1);
        eventPayloadNode.put("eventTime", 1648496000);
        eventPayloadNode.put("currentUserId", "");

        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("test", "async body");
        eventPayloadNode.set("body", bodyNode);


        this.messageBroker.setShouldTransformPayload(false);
        this.messageBroker.handle(objectMapper.writeValueAsString(eventPayloadNode));
    }

    @Test
    public void testPayloadPassedDirectlyToHandler_ShouldSucceed() throws IOException {
        // Create sample payload
        ObjectNode eventPayloadNode = objectMapper.createObjectNode();

        // Set up the properties directly on the ObjectNode
        eventPayloadNode.put("key", "pagebuilder:VERIFY_EMAIL");
        eventPayloadNode.put("time", "1679696377"); // March 24th, 2023 in Epoch Seconds

        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("someKey", "someValue");
        eventPayloadNode.set("body", bodyNode);

        this.messageBroker.setShouldTransformPayload(false);
        this.messageBroker.handle(objectMapper.writeValueAsString(eventPayloadNode));

        assertEquals("TestAsync", calledHandlerName);
        assertEquals(2, calledPayload.getVersion());
        assertEquals(1, calledPayload.getTypeId());

        // Java's internal date class subtracts 1900 from the year when calling `getYear()`
        assertEquals(2023, calledPayload.getTime().getYear() + 1900);

        assertEquals(2, calledPayload.getTime().getMonth()); // Month is 0 indexed
        assertEquals(24, calledPayload.getTime().getDate());

        assertEquals("someValue", calledPayload.getBody().get("someKey").asText());
    }

    @Test
    public void testPayloadPassedDirectlyToHandlerWithUtf8Chars_ShouldSucceed() throws IOException {
        // Create sample payload
        ObjectNode eventPayloadNode = objectMapper.createObjectNode();

        // Set up the properties directly on the ObjectNode
        eventPayloadNode.put("key", "pagebuilder:VERIFY_EMAIL");
        eventPayloadNode.put("time", "1679696377"); // March 24th, 2023 in Epoch Seconds

        ObjectNode bodyNode = objectMapper.createObjectNode();
        bodyNode.put("aUtf8Char", "€");
        eventPayloadNode.set("body", bodyNode);

        this.messageBroker.setShouldTransformPayload(false);
        this.messageBroker.handle(objectMapper.writeValueAsString(eventPayloadNode));

        assertEquals("TestAsync", calledHandlerName);
        assertEquals("€", calledPayload.getBody().get("aUtf8Char").asText());
    }


    private void setupRequestHandlers() {
        // Add test handlers
        List<RequestHandler> requestHandlers = new ArrayList<>();
        requestHandlers.add(new CartAddRequestInterceptor());
        requestHandlers.add(new TestSyncBefore());
        requestHandlers.add(new CartAddResponseInterceptor());
        requestHandlers.add(new TestSyncAfter());
        requestHandlers.add(new LegacySampleEndpoint());
        requestHandlers.add(new SampleEndpoint());
        // Set handlers
        messageBroker.setRequestHandlers(requestHandlers);
    }

    private void setupEventHandlers() {
        // Add test handlers
        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new TestAsync());
        // Set handlers
        messageBroker.setEventHandlers(eventHandlers);
    }
}

