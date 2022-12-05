package com.arcxp.platform.sdk.http;

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Arc Commerce Http Client for calls to Commerce Apis.
 */
public interface ArcHttpClient {


    /**
     * GET request with an unstructured response.
     *
     * @param url     The url to call
     * @param headers The additional headers to be sent with the request
     * @return unstructured ObjectNode return
     */
    Response<ObjectNode> get(String url, Map<String, String> headers);

    /**
     * GET request with a typed(structured) response.
     *
     * @param <T>           The type of response object
     * @param url           The url to call
     * @param headers       The additional headers to be sent with the request
     * @param responseClass The class of the response object
     * @return The response with data populated in the response class
     */
    <T> Response<T> get(String url, Map<String, String> headers, Class<T> responseClass);

    /**
     * POST request with an unstructured response.
     *
     * @param url     The url to call
     * @param headers The additional headers to be sent with the request
     * @param json    The body of the request
     * @return unstructured ObjectNode return
     */
    Response<ObjectNode> post(String url, Map<String, String> headers, Object json);

    /**
     * Multipart Form POST request with an unstructured response.
     *
     * @param url     The url to call
     * @param headers The additional headers to be sent with the request
     * @param data    Multipart Form Data body of the request
     * @return unstructured ObjectNode return
     */
    Response<ObjectNode> post(String url, Map<String, String> headers, MultiPartFormData data);

    /**
     * POST request with a typed(structured) response.
     *
     * @param <T>           The type of response object
     * @param url           The url to call
     * @param headers       The additional headers to be sent with the request
     * @param json          The body of the request
     * @param responseClass The class of the response object
     * @return The response with data populated in the response class
     */
    <T> Response<T> post(String url, Map<String, String> headers, Object json, Class<T> responseClass);

    /**
     * Multipart Form POST request with a typed(structured) response.
     *
     * @param <T>           The type of response object
     * @param url           The url to call
     * @param headers       The additional headers to be sent with the request
     * @param data          Multipart Form Data body of the request
     * @param responseClass The class of the response object
     * @return The response with data populated in the response class
     */
    <T> Response<T> post(String url, Map<String, String> headers, MultiPartFormData data,
                         Class<T> responseClass);

    /**
     * PUT request with an unstructured response.
     *
     * @param url     The url to call
     * @param headers The additional headers to be sent with the request
     * @param json    The body of the request
     * @return unstructured ObjectNode return
     */
    Response<ObjectNode> put(String url, Map<String, String> headers, Object json);

    /**
     * PUT request with a typed(structured) response.
     *
     * @param <T>           The type of response object
     * @param url           The url to call
     * @param headers       The additional headers to be sent with the request
     * @param json          The body of the request
     * @param responseClass The class of the response object
     * @return The response with data populated in the response class
     */
    <T> Response<T> put(String url, Map<String, String> headers, Object json, Class<T> responseClass);


    /**
     * DELETE request with an unstructured response.
     *
     * @param url     The url to call
     * @param headers The additional headers to be sent with the request
     * @return unstructured ObjectNode return
     */
    Response<ObjectNode> delete(String url, Map<String, String> headers);


    /**
     * DELETE request with a typed(structured) response.
     *
     * @param <T>           The type of response object
     * @param url           The url to call
     * @param headers       The additional headers to be sent with the request
     * @param responseClass The class of the response object
     * @return The response with data populated in the response class
     */
    <T> Response<T> delete(String url, Map<String, String> headers, Class<T> responseClass);

}
