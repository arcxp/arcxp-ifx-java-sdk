package com.arcxp.platform.sdk.http;

import com.arcxp.platform.sdk.utils.ArcHelper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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

import java.nio.charset.StandardCharsets;
import java.util.Map;

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
        HttpGet get = new HttpGet(constructFullyQualifiedUrl(url));
        return request(get, headers, responseClass);
    }

    public Response<ObjectNode> get(String url, Map<String, String> headers) {
        return get(url, headers, ObjectNode.class);
    }

    @Override
    public <T> Response<T> post(String url, Map<String, String> headers, Object json, Class<T> responseClass) {
        HttpPost post = new HttpPost(constructFullyQualifiedUrl(url));
        addPayload(json, post);
        return request(post, headers, responseClass);
    }

    public Response<ObjectNode> post(String url, Map<String, String> headers, Object json) {
        return post(url, headers, json, ObjectNode.class);
    }

    @Override
    public <T> Response<T> post(String url, Map<String, String> headers, MultiPartFormData data,
                                Class<T> responseClass) {
        HttpPost post = new HttpPost(constructFullyQualifiedUrl(url));
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
        HttpPut put = new HttpPut(constructFullyQualifiedUrl(url));
        addPayload(json, put);
        return request(put, headers, responseClass);
    }

    public Response<ObjectNode> put(String url, Map<String, String> headers, Object json) {
        return put(url, headers, json, ObjectNode.class);
    }

    @Override
    public <T> Response<T> delete(String url, Map<String, String> headers, Class<T> responseClass) {
        return request(new HttpDelete(constructFullyQualifiedUrl(url)), headers, responseClass);
    }

    public Response<ObjectNode> delete(String url, Map<String, String> headers) {
        return delete(url, headers, ObjectNode.class);
    }

    /**
     * Adds a JSON payload to the provided HTTP request. The payload can be a JSON object or a JSON-formatted string.
     * The method sets the content type of the payload to `application/json` and encodes it using UTF-8.
     *
     * @param json    The JSON object or JSON-formatted string to be added as the payload.
     *                If the object is not a string, it will be converted to a JSON string using an ObjectMapper.
     * @param request The HTTP request to which the JSON payload should be added.
     *                This request must be capable of enclosing an entity (i.e., a POST or PUT request).
     * @throws JsonProcessingException If the provided object cannot be converted to a JSON string.
     *                                 This exception is caught internally and logged as an error.
     */
    private void addPayload(Object json, HttpEntityEnclosingRequestBase request) {
        try {
            String jsonStr = null;
            if (json instanceof String) {
                jsonStr = (String) json;
            } else {
                jsonStr = objectMapper.writeValueAsString(json);
            }
            StringEntity entity = new StringEntity(jsonStr, StandardCharsets.UTF_8);
            entity.setContentType("application/json");
            request.setEntity(entity);
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
            String arcToken = getArcToken();
            if (StringUtils.isEmpty(arcToken)) {
                LOG.warn("Arc Bearer Token not found.");
            } else {
                request.addHeader("Authorization", "Bearer " + arcToken);
            }
        } else {
            request.addHeader("arc-organization", env.getProperty("org"));
            request.addHeader("arc-v2-username", "api");
        }
        request.addHeader("Arc-Site", env.getProperty("site"));
    }

    /**
     * Check for a Personal Access Token at the IFX 2.0 expected path.
     */
    private String getArcToken() {
        String arcToken = env.getProperty("PERSONAL_ACCESS_TOKEN");

        if (StringUtils.isEmpty(arcToken)) {
            arcToken = env.getProperty("personal.access.token");
        }

        return arcToken;
    }

    /**
     * Sends an HTTP request and returns a generic type {@code Response} containing the response data.
     * This method adds provided headers to the request, executes the request using a {@code CloseableHttpClient},
     * and processes the response by converting it into an instance of the specified {@code responseClass}.
     *
     * @param <T>           the type of the response data expected
     * @param request       the {@code HttpUriRequest} to be executed
     * @param headers       a map of header names to values to be added to the request
     * @param responseClass the {@code Class} object corresponding to the type {@code T}
     * @return a {@code Response} object with the status code and data set accordingly
     * @throws JsonParseException if the response cannot be parsed into an {@code ObjectNode}
     *                            or the specified type {@code T}
     * @throws IOException        if an I/O error occurs while sending the request or reading the response
     */
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
                if (ArrayUtils.isEmpty(content)) {
                    arcResponse.setData(null);
                } else if (responseClass.isAssignableFrom(ObjectNode.class)) {
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

    /**
     * Constructs a complete URL from a given path or returns the URL if already complete.
     * If the input is a path (starting with "/"), it prepends the protocol (http or https
     * based on a property) and the host to the path. If the input is already a complete URL,
     * it is returned as-is.
     *
     * @param pathOrUrl the path (starting with "/") or the complete URL to process
     * @return the complete URL as a {@code String}
     */

    private String constructFullyQualifiedUrl(String pathOrUrl) {
        // Check if it's a path
        if (pathOrUrl.startsWith("/")) {
            String protocol = env.getProperty("hostSecure", Boolean.class, true) ? "https" : "http";
            return protocol + "://" + env.getProperty("host") + pathOrUrl;
        }
        return pathOrUrl;
    }
}








