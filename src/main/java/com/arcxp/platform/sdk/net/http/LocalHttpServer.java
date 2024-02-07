package com.arcxp.platform.sdk.net.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.amazonaws.util.StringUtils;
import com.arcxp.platform.sdk.broker.MessageBroker;
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
                                Map<String, String> payload = objectMapper.readValue(reqEnt, Map.class);
                                for (NameValuePair param : parameters) {
                                    if (!StringUtils.isNullOrEmpty(param.getName())) {
                                        payload.put(param.getName(), param.getValue());
                                    }
                                }
                                payload.put(TYPE_ID_NAME,
                                        (payload.get(ASYNC_NAME).equalsIgnoreCase("true") ? EVENT_PAYLOAD_TYPE_ID
                                            : REQUEST_PAYLOAD_TYPE_ID));
                                payload.put(VERSION_NAME, "2");
                                String msg = objectMapper.writeValueAsString(payload);
                                String responseBody = messageBroker.handle(msg);
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
        params.add(new BasicNameValuePair(EVENT_NAME, eventName));
        return params;
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
