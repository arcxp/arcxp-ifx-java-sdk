package com.arcxp.platform.sdk.http;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.arcxp.platform.sdk.utils.ArcHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Default Implementation of Arc HTTP Client based on Apache Http Components.
 */
public class DefaultArcHttpClient implements ArcHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultArcHttpClient.class);

    private final CloseableHttpClient httpclient;

    private final ObjectMapper objectMapper;

    private final Environment env;

    public DefaultArcHttpClient(Environment env, ObjectMapper objectMapper) {
        if (Boolean.parseBoolean(env.getProperty("tracing"))) {
            this.httpclient = com.amazonaws.xray.proxies.apache.http.HttpClientBuilder.create().build();
        } else {
            this.httpclient = HttpClientBuilder.create().build();
        }

        this.env = env;

        this.objectMapper = objectMapper;
        LOG.info("Arc HTTP Client Created");

    }

    @Override
    public <T> Response<T> get(String url, Map<String, String> headers, Class<T> responseClass) {
        HttpGet get = new HttpGet(completeUrl(url));
        return request(get, headers, responseClass);
    }

    public Response<ObjectNode> get(String url, Map<String, String> headers) {
        return get(url, headers, ObjectNode.class);
    }

    @Override
    public <T> Response<T> post(String url, Map<String, String> headers, Object json, Class<T> responseClass) {
        HttpPost post = new HttpPost(completeUrl(url));
        addPayload(json, post);
        return request(post, headers, responseClass);
    }

    public Response<ObjectNode> post(String url, Map<String, String> headers, Object json) {
        return post(url, headers, json, ObjectNode.class);
    }

    @Override
    public <T> Response<T> post(String url, Map<String, String> headers, MultiPartFormData data,
                                Class<T> responseClass) {
        HttpPost post = new HttpPost(completeUrl(url));
        HttpEntity entity = null;
        if (data.getContentType().equals(MultiPartFormData.ContentType.APPLICATION_JSON)) {
            entity = MultipartEntityBuilder.create()
                .addPart(data.getName(), new StringBody(data.getBody().toString(), ContentType.APPLICATION_JSON))
                .build();
        } else {
            throw new RuntimeException("invalid content type");
        }

        post.setEntity(entity);
        return request(post, headers, responseClass);
    }

    @Override
    public Response<ObjectNode> post(String url, Map<String, String> headers, MultiPartFormData data) {
        return post(url, headers, data, ObjectNode.class);
    }

    @Override
    public <T> Response<T> put(String url, Map<String, String> headers, Object json, Class<T> responseClass) {
        HttpPut put = new HttpPut(completeUrl(url));
        addPayload(json, put);
        return request(put, headers, responseClass);
    }

    public Response<ObjectNode> put(String url, Map<String, String> headers, Object json) {
        return put(url, headers, json, ObjectNode.class);
    }

    @Override
    public <T> Response<T> delete(String url, Map<String, String> headers, Class<T> responseClass) {
        return request(new HttpDelete(completeUrl(url)), headers, responseClass);
    }

    public Response<ObjectNode> delete(String url, Map<String, String> headers) {
        return delete(url, headers, ObjectNode.class);
    }

    private void addPayload(Object json, HttpEntityEnclosingRequestBase request) {
        try {
            String jsonStr = null;
            if (json instanceof String) {
                jsonStr = (String) json;
            } else {
                jsonStr = objectMapper.writeValueAsString(json);
            }
            StringEntity entity = new StringEntity(jsonStr);
            entity.setContentType("application/json");
            request.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error Creating payload for request body: {}", json);
        } catch (JsonProcessingException e) {
            LOG.error("Error Writing Payload to JSON.", e);
        }
    }

    private void addHeaders(HttpUriRequest request, Map<String, String> headers) {
        if (request != null) {
            addArcAuthHeaders(request);
            if (headers != null) {
                headers.entrySet().stream().forEach((entry) -> {
                    request.addHeader(entry.getKey(), entry.getValue());
                });
            }
        }
    }

    private void addArcAuthHeaders(HttpUriRequest request) {

        if (request.getURI() != null && ArcHelper.isArcAPI(request.getURI().getHost())) {
            request.addHeader("Authorization", "Bearer " + env.getProperty("personal.access.token"));
        } else {
            request.addHeader("arc-organization", env.getProperty("org"));
            request.addHeader("arc-v2-username", "api");
        }
        request.addHeader("Arc-Site", env.getProperty("site"));
    }

    private <T> Response<T> request(HttpUriRequest request, Map<String, String> headers, Class<T> responseClass) {

        Response arcResponse = new Response();
        byte[] content = null;
        try {
            addHeaders(request, headers);

            try (CloseableHttpResponse response = this.httpclient.execute(request)) {
                int code = response.getStatusLine().getStatusCode();
                arcResponse.setStatus(code);
                HttpEntity entityResponse = response.getEntity();
                content = EntityUtils.toByteArray(entityResponse);
                if (responseClass.isAssignableFrom(ObjectNode.class)) {
                    ObjectNode node = (ObjectNode) objectMapper.readTree(content);
                    node.put("httpStatusCode", code);
                    arcResponse.setData(node);
                } else {
                    T result = objectMapper.readValue(content, responseClass);
                    arcResponse.setData(result);
                }
            }
        } catch (JsonParseException e) {
            LOG.error("Failed to parse response. Status {}. Response {}.", arcResponse.getStatus(),
                new String(content, StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("Error sending arc http request", e);
        }

        return arcResponse;
    }

    private String completeUrl(String pathOrUrl) {
        // Check if it's a path
        if (pathOrUrl.startsWith("/")) {
            String protocol = env.getProperty("hostSecure", Boolean.class, true) ? "https" : "http";
            return protocol + "://" + env.getProperty("host") + pathOrUrl;
        }
        return pathOrUrl;
    }
}








