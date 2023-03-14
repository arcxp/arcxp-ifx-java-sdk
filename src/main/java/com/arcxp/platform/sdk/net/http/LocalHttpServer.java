package com.arcxp.platform.sdk.net.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.amazonaws.util.StringUtils;
import com.arcxp.platform.sdk.broker.MessageBroker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalHttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalHttpServer.class);
    private static final String INVOKE_HANDLER_PATH = "/ifx/local/invoke/*";
    public static final String EVENT_NAME = "eventName";
    public static final String VERSION_NAME = "version";
    public static final String TYPE_ID_NAME = "typeId";
    public static final String ASYNC_NAME = "async";
    public static final String UUID_NAME = "uuid";
    public static final String USER_ID_NAME = "currentUserId";
    public static final String ATTRIBUTE_BODY_NAME = "body";
    public static final String EVENT_PAYLOAD_TYPE_ID = "1";
    public static final String REQUEST_PAYLOAD_TYPE_ID = "5";
    private final HttpServer server;
    @Autowired
    private ObjectMapper objectMapper;

    public LocalHttpServer(MessageBroker messageBroker) {
        server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .registerHandler(INVOKE_HANDLER_PATH, new HttpRequestHandler() {
                    @Override
                    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
                            throws HttpException, IOException {
                        try {
                            if (request instanceof HttpEntityEnclosingRequest
                                    && request.getRequestLine().getMethod().equalsIgnoreCase("POST")) {

                                HttpEntity requestEntity = ((HttpEntityEnclosingRequest) request).getEntity();
                                String reqEnt = EntityUtils.toString(requestEntity);
                                List<NameValuePair> parameters = getParameters(request);
                                Map<String, String> inboundPayload = objectMapper.readValue(reqEnt, Map.class);
                                if (inboundPayload.containsKey(ATTRIBUTE_BODY_NAME)) {
                                    LOG.error("Please use POST with URL params to /ifx/local/invoke/{eventname}?"
                                            + "{attribute=value} for local testing");
                                    response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                                }
                                String responseBody = messageBroker.handle(buildOutboundPayload(parameters,
                                        inboundPayload));
                                StringEntity responseEntity = new StringEntity(responseBody);
                                response.setEntity(responseEntity);
                                response.setStatusCode(HttpStatus.SC_OK);
                            } else {
                                LOG.error("Please use POST with body to /ifx/local/invoke/{eventname}?"
                                    + "{attribute=value} for local testing");
                                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                            }
                        } catch (Exception e) {
                            LOG.error("Local Error:", e);
                            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
                })
                .create();
    }

    private List<NameValuePair> getParameters(final HttpRequest request) {
        URI uri = URI.create(request.getRequestLine().getUri());
        // Get the event name from the path
        String eventName = Paths.get(uri.getPath()).getFileName().toString();
        List<NameValuePair> params = URLEncodedUtils.parse(
                uri,
                Charset.forName("UTF-8"));
        // This ensures that IF a user specified eventName as a URL param, the integration:event specified in the invoke
        // will be the final eventName in the list therefore the only one used.
        params.add(new BasicNameValuePair(EVENT_NAME, eventName));
        return params;
    }

    private String buildOutboundPayload(List<NameValuePair> parameters,
                                                      Map<String, String> inboundPayload) {
        Map<String, Object> outboundPayload = new HashMap<>();
        for (NameValuePair param : parameters) {
            if (!StringUtils.isNullOrEmpty(param.getName())) {
                outboundPayload.put(param.getName(), param.getValue());
            }
        }

        if (!outboundPayload.containsKey(ASYNC_NAME)) {
            outboundPayload.put(TYPE_ID_NAME, EVENT_PAYLOAD_TYPE_ID);
        } else {
            outboundPayload.put(TYPE_ID_NAME,
                    (outboundPayload.get(ASYNC_NAME).toString().equalsIgnoreCase("true") ? EVENT_PAYLOAD_TYPE_ID
                            : REQUEST_PAYLOAD_TYPE_ID));
        }
        if (!outboundPayload.containsKey(UUID_NAME)) {
            outboundPayload.put(VERSION_NAME, "");
        }
        if (!outboundPayload.containsKey(USER_ID_NAME)) {
            outboundPayload.put(VERSION_NAME, "");
        }
        outboundPayload.put(VERSION_NAME, "2");
        outboundPayload.put(ATTRIBUTE_BODY_NAME, inboundPayload);
        String msg = null;
        try {
            msg = objectMapper.writeValueAsString(outboundPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return msg;
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
