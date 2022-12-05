package com.arcxp.platform.sdk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import javax.inject.Inject;

import com.arcxp.platform.sdk.broker.MessageBroker;
import com.arcxp.platform.sdk.http.ArcHttpClient;
import com.arcxp.platform.sdk.http.DefaultArcHttpClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;


/**
 * This class creates the entry point to the lambda calls via the handler.
 * Beans within this creates several shared services such as http clients and serialization services.
 */

@Configuration
@PropertySource(value = "classpath:secret-${ENV:local}.properties", ignoreResourceNotFound = true)
public class FunctionConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(FunctionConfiguration.class);
    @Inject
    private MessageBroker messageBroker;

    /**
     * Function Handler for Integration Lambda Invoke.
     *
     * @return Lambda Response
     */
    @Profile("!local")
    @Bean
    public Function<Message<String>, Message<String>> handler() {
        return value -> {
            MessageHeaders headers = value.getHeaders();
            if (headers != null) {
                MDC.put("AWSRequestId", headers.get("lambda-runtime-aws-request-id", String.class));
            }
            String response = this.messageBroker.handle(value.getPayload());
            MDC.remove("AWSRequestId");
            return new GenericMessage<String>(response);
        };
    }

    /**
     * Shared Object Mapper for Serialization.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .build();
        return objectMapper;
    }

    /**
     * Arc Commerce Http Client with warmup function.
     *
     * @param objectMapper ObjectMapper for Serialization
     * @param env          The Environment
     * @return Arc Commerce Http Client
     */
    @Bean
    public ArcHttpClient arcHttpClient(ObjectMapper objectMapper, Environment env) {
        final ArcHttpClient arcHttpClient = new DefaultArcHttpClient(env, objectMapper);
        //Warmup
        if (env.getProperty("arc.http.warmup", Boolean.class, Boolean.FALSE)) {
            Future warmupFuture = Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    arcHttpClient.get("/sales/api/v1/swagger.json", null);
                } catch (Exception e) {
                    LOG.warn("Error Warming Up Arc HTTP Hosts", e);
                }
            });

            try {
                warmupFuture.get(20, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOG.warn("Arc HTTP Warmup Timed out", e);
            }
        }

        return arcHttpClient;
    }

}
