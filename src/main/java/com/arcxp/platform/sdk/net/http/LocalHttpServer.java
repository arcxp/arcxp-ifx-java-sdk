package com.arcxp.platform.sdk.net.http;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.arcxp.platform.sdk.broker.MessageBroker;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalHttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(LocalHttpServer.class);
    
    private final HttpServer server;

    public LocalHttpServer(MessageBroker messageBroker) {
        server = ServerBootstrap.bootstrap()
                    .setListenerPort(8080)
                    .registerHandler("/ifx/local/invoke", new HttpRequestHandler() {
                        @Override
                        public void handle(HttpRequest request, HttpResponse response, HttpContext context)
                            throws HttpException, IOException {
                            try {
                                if (request instanceof HttpEntityEnclosingRequest 
                                        && request.getRequestLine().getMethod().equalsIgnoreCase("POST")) {
                                    HttpEntity requestEntity = ((HttpEntityEnclosingRequest) request).getEntity();
                                    String responseBody = messageBroker.handle( EntityUtils.toString(requestEntity));
                                    StringEntity responseEntity = new StringEntity(responseBody);
                                    response.setEntity(responseEntity);
                                    response.setStatusCode(HttpStatus.SC_OK);
                                } else {
                                    LOG.error("Please use POST with body to /ifx/local/invoke for local testing");
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
