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
        }
    }

    @ArcEndpoint("some/url/1")
    private class LegacySampleEndpoint extends RequestHandler {
        @Override
        public void handle(RequestPayload payload) {
            calledHandlerName = "LegacySampleEndpoint";
        }
    }

    @ArcSyncEvent("some/url/2")
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
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        FunctionConfiguration functionConfig = new FunctionConfiguration();
        this.objectMapper = functionConfig.objectMapper();
        calledHandlerName = null;
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

    @Test
    public void testNamespaceExists() throws IOException {
        // Create sample payload
        String eventPayloadJson = "{\"eventType\":\"pagebuilder:VERIFY_EMAIL\",\"eventTime\": \"\"}";

        ObjectNode node = (ObjectNode) this.objectMapper.readTree(eventPayloadJson);
        when(this.objectMapperMock.readTree(anyString())).thenReturn(node);

        this.messageBroker.handle(eventPayloadJson);

        assertEquals("TestAsync", calledHandlerName);
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

