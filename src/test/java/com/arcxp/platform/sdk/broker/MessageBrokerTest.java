package com.arcxp.platform.sdk.broker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.arcxp.platform.sdk.FunctionConfiguration;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class MessageBrokerTest {

    @InjectMocks
    private MessageBroker messageBroker;

    @Mock
    private ObjectMapper objectMapperMock;

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
        FunctionConfiguration functionConfig = new FunctionConfiguration();
        this.objectMapper = functionConfig.objectMapper();
        calledHandlerName = null;
        calledPayload = null;
        setRequestHandlers();
        setEventHandlers();
    }


    @Test
    public void testCartAddRequestInterceptor() throws IOException {
        // Create sample payload
        String requestPayloadJson = "{\"key\":\"CART_ADD\", \"typeId\":2, \"uuid\": \"\", \"uri\": \"\", "
            + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);

        assertEquals("CartAddRequestInterceptor", calledHandlerName);
    }

    @Test
    public void testTestSyncBefore() throws IOException {
        // Create sample payload
        String requestPayloadJson = "{\"key\":\"SYNC_TEST\", \"typeId\":2, \"uuid\": \"\", \"uri\": \"\", "
            + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);

        assertEquals("TestSyncBefore", calledHandlerName);
    }

    @Test(expected = EventPayloadException.class)
    public void testRequestInterceptorShouldThrowForV2() throws IOException {
        // Create sample payload
        String requestPayloadJson =
            "{\"eventName\":\"commerce:SYNC_TEST\", \"version\": 2, \"typeId\":2, \"uuid\": \"\", "
                + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);
    }

    @Test
    public void testCartAddResponseInterceptor() throws IOException {
        // Create sample payload
        String requestPayloadJson = "{\"key\":\"CART_ADD\", \"typeId\":3, \"uuid\": \"\", \"uri\": \"\", "
            + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);

        assertEquals("CartAddResponseInterceptor", calledHandlerName);
    }

    @Test
    public void testTestSyncAfter() throws IOException {
        // Create sample payload
        String requestPayloadJson = "{\"key\":\"SYNC_TEST\", \"typeId\":3, \"uuid\": \"\", \"uri\": \"\", "
            + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);

        assertEquals("TestSyncAfter", calledHandlerName);
    }

    @Test(expected = EventPayloadException.class)
    public void testResponseInterceptorShouldThrowForV2() throws IOException {
        // Create sample payload
        String requestPayloadJson =
            "{\"eventName\":\"commerce:SYNC_TEST\", \"version\": 2, \"typeId\":3, \"uuid\": \"\", "
                + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);
    }

    @Test
    public void testLegacySampleEndpoint() throws IOException {
        // Create sample payload
        String requestPayloadJson = "{\"key\":\"\", \"typeId\":4, \"uuid\": \"\", \"uri\": \"some/url/1\", "
            + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);

        assertEquals("LegacySampleEndpoint", calledHandlerName);
    }

    @Test
    public void testSampleEndpoint() throws IOException {
        // Create sample payload
        String requestPayloadJson = "{\"key\":\"\", \"typeId\":4, \"uuid\": \"\", \"uri\": \"some/url/2\", "
            + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);

        assertEquals("SampleEndpoint", calledHandlerName);
    }

    @Test(expected = EventPayloadException.class)
    public void testCustomEndpointShouldThrowForV2() throws IOException {
        // Create sample payload
        String requestPayloadJson =
            "{\"eventName\":\"commerce:some/url\", \"version\": 2, \"typeId\": 4, \"uuid\": \"\", "
                + "\"currentUserId\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);
    }

    public void testSyncEventV2() throws IOException {
        // Create sample payload
        String requestPayloadJson =
            "{\"eventName\":\"commerce:some/url\", \"version\": 2, \"typeId\": 5, \"uuid\": \"uuid123\", "
                + "\"currentUserId\": \"userid123\", \"body\": {\"test\": \"sync body\"}}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(requestPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(requestPayloadJson);
        assertEquals("SampleEndpoint", calledHandlerName);
        assertEquals(2, calledPayload.getVersion());
        assertEquals(5, calledPayload.getTypeId());
        assertEquals("uuid123", calledPayload.getUuid());
        assertEquals("userid123", ((RequestPayload) calledPayload).getCurrentUserId());
        assertEquals("sync body", calledPayload.getBody().get("test").asText());
    }

    @Test
    public void testAsyncEventWithNamespace() throws IOException {
        // Create sample payload
        String eventPayloadJson = "{\"eventType\":\"pagebuilder:VERIFY_EMAIL\",\"eventTime\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(eventPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(eventPayloadJson);

        assertEquals("TestAsync", calledHandlerName);
    }

    @Test
    public void testAsyncEventV2WithNamespace() throws IOException {
        // Create sample payload
        String eventPayloadJson =
            "{\"eventName\": \"pagebuilder:VERIFY_EMAIL\", \"version\": 2, \"typeId\": 1, \"eventTime\": 1648496000, "
                + "\"body\": {\"test\": \"async body\"}}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(eventPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(eventPayloadJson);

        assertEquals("TestAsync", calledHandlerName);
        assertEquals(2, calledPayload.getVersion());
        assertEquals(1, calledPayload.getTypeId());
        assertEquals(1648496000, calledPayload.getTime().getTime());
        assertEquals("async body", calledPayload.getBody().get("test").asText());
    }

    private void setRequestHandlers() {
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

    private void setEventHandlers() {
        // Add test handlers
        List<EventHandler> eventHandlers = new ArrayList<>();
        eventHandlers.add(new TestAsync());
        // Set handlers
        messageBroker.setEventHandlers(eventHandlers);
    }
}

