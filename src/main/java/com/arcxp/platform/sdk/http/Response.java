package com.arcxp.platform.sdk.http;

/**
 * Arc Http Client Generic Response.
 *
 * @param <T> Response Data Class
 */
public class Response<T> {
    private T data;
    private int status;

    /**
     * The structured response data.
     *
     * @return Object holding response data
     */
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * The HTTP Response Code.
     *
     * @return http response code
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
