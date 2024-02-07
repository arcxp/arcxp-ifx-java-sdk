package com.arcxp.platform.sdk.net.http;

import com.arcxp.platform.sdk.broker.MessageBroker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
@Profile("local")
@SuppressWarnings("checkstyle:linelength")
public class LocalHttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalHttpServer.class);
    private static final String INVOKE_HANDLER_PATH = "/ifx/local/invoke";
    public static final String VERSION_NAME = "version";
    public static final String TYPE_ID_NAME = "typeId";
    public static final String ASYNC_NAME = "async";
    public static final String EVENT_PAYLOAD_TYPE_ID = "1";
    public static final String REQUEST_PAYLOAD_TYPE_ID = "5";
    private final HttpServer server;
    @Autowired
    private ObjectMapper objectMapper;

    public LocalHttpServer(MessageBroker messageBroker) {
        messageBroker.setShouldTransformPayload(false);
        messageBroker.setShouldPropogateExceptions(true);
        server = ServerBootstrap
                .bootstrap()
                .setListenerPort(8080)
                .registerHandler(INVOKE_HANDLER_PATH, new HttpRequestHandler() {
                    @Override
                    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws JsonProcessingException {
                        try {
                            if (request instanceof HttpEntityEnclosingRequest && request.getRequestLine().getMethod().equalsIgnoreCase("POST")) {

                                // Construct the payload to send to the handler
                                HttpEntity requestEntity = ((HttpEntityEnclosingRequest) request).getEntity();
                                String reqEnt = EntityUtils.toString(requestEntity);
                                Map<String, String> payload = objectMapper.readValue(reqEnt, Map.class);
                                String msg = objectMapper.writeValueAsString(payload);

                                // Send payload to the handler
                                String responseBody = messageBroker.handle(msg);
                                LOG.info("Response Body: {}", responseBody);

                                // Extract 'body' content from the response
                                JsonNode rootNode = objectMapper.readTree(responseBody);

                                // Check for an error in the response
                                JsonNode errorNode = rootNode.get("error");
                                if (errorNode != null && !errorNode.isNull()) {
                                    // An error exists, so return this in the response instead of the body
                                    StringEntity errorResponseEntity = new StringEntity(errorNode.toString(), ContentType.APPLICATION_JSON);
                                    response.setEntity(errorResponseEntity);
                                    response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                                } else {
                                    // No error, so proceed with the body
                                    JsonNode bodyNode = rootNode.get("body");
                                    if (bodyNode == null) {
                                        throw new IllegalStateException("The 'body' key is missing in the response.");
                                    }

                                    // Prepare the response entity with the body content
                                    StringEntity responseEntity = new StringEntity(bodyNode.toString(), ContentType.APPLICATION_JSON);
                                    response.setEntity(responseEntity);
                                    response.setStatusCode(HttpStatus.SC_OK);
                                }
                            } else {
                                LOG.error("Please use POST with body to '/ifx/local/invoke' for local testing. 'key' is a required property.");
                                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                                Map<String, String> error = Collections.singletonMap("message", "Please use POST with body to '/ifx/local/invoke'. 'key' is a required property.");
                                response.setEntity(new StringEntity(objectMapper.writeValueAsString(error), ContentType.APPLICATION_JSON));
                            }
                        } catch (Exception e) {
                            LOG.error("Local Error:", e);
                            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            Map<String, String> error = Collections.singletonMap("message", e.getMessage());
                            response.setEntity(new StringEntity(objectMapper.writeValueAsString(error), ContentType.APPLICATION_JSON));
                        }
                    }
                }).create();
    }

    @PostConstruct
    public void init() throws IOException {
        server.start();
        LOG.info("Local Server Started at http://localhost:8080/ifx/local/invoke");
    }

    @PreDestroy
    public void stopServer() {
        if (this.server != null) {
            this.server.stop();
        }
    }
}
