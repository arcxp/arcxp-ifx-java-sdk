package com.arcxp.platform.sdk;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.amazonaws.services.lambda.runtime.Context;
import com.arcxp.platform.sdk.broker.MessageBroker;
import com.arcxp.platform.sdk.http.ArcHttpClient;
import com.arcxp.platform.sdk.http.DefaultArcHttpClient;
import com.datadoghq.datadog_lambda_java.DDLambda;
import com.datadoghq.datadog_lambda_java.Headerable;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            String keyArn = System.getenv("DD_API_KEY_SECRET_ARN");
 
            if (keyArn != null && !keyArn.isEmpty()) {
                Context context = value.getHeaders().get("aws-context", Context.class);

                Headerable headerPlayload = new Headerable() {

                    @Override
                    public Map<String, String> getHeaders() {
                        return value.getHeaders().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
                    }

                    @Override
                    public void setHeaders(Map<String, String> headers) {
                    }

                };

                DDLambda ddl = new DDLambda(headerPlayload, context);
            }

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
