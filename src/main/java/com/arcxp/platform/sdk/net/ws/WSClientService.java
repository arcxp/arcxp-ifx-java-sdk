package com.arcxp.platform.sdk.net.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;

import com.arcxp.platform.sdk.broker.MessageBroker;
import com.arcxp.platform.sdk.net.NetClient;
import com.google.common.collect.ImmutableMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.arcxp.platform.sdk.utils.ArcHelper.isArcAPI;

/**
 * Websocket Implementation class for debug websocket.
 */

@Component
@Profile("local")
public final class WSClientService {


    private final WsClient client;
    Logger logger = LoggerFactory.getLogger(WSClientService.class);

    @Autowired
    public WSClientService(Environment env, MessageBroker messageBroker) throws URISyntaxException {
        this.client = new WsClient(env, messageBroker);
    }

    @PostConstruct
    public void init() {
        Executors.newSingleThreadExecutor().execute(() -> {

            this.client.setConnectionLostTimeout(10000);
            this.client.connect();
        });
    }

    public class WsClient extends WebSocketClient implements NetClient {

        private final MessageBroker messageBroker;

        private final String developerKey;

        private Environment env;

        public WsClient(Environment env, MessageBroker messageBroker) throws URISyntaxException {
            super(new URI(WebSocketUtil.buildWSUri(env.getProperty("host"), env.getProperty("app"))),
                isArcAPI(env.getProperty("host"))
                    ? ImmutableMap.<String, String>builder()
                    .put("Authorization", "Bearer " + env.getProperty("personal.access.token"))
                    .put("Arc-Site", env.getProperty("site"))
                    .build()
                    : ImmutableMap.<String, String>builder()
                    .put("arc-organization", env.getProperty("org"))
                    .put("Arc-Site", env.getProperty("site"))
                    .put("arc-v2-username", env.getProperty("developerKey"))
                    .build());
            this.messageBroker = messageBroker;
            this.developerKey = env.getProperty("developerKey");
        }


        @Override
        public void onOpen(ServerHandshake handshake) {
            logger.info("connected");
            send("{\"developerKey\":\"" + developerKey + "\"}");
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    send("{\"developerKey\":\"" + developerKey + "\"}");
                }
            }, 0, 8000);
        }


        @Override
        public void onClose(int code, String reason, boolean remote) {
            logger.info("disconnected with exit code " + code + " additional info: " + reason);
        }

        @Override
        public void onMessage(String message) {
            receive(message);
        }

        @Override
        public void onError(Exception ex) {
            logger.error("an error occurred:" + ex);
        }

        @Override
        public void receive(String message) {
            String response = messageBroker.handle(message);
            if (response != null) {
                send(response);
            }

        }
    }

}
