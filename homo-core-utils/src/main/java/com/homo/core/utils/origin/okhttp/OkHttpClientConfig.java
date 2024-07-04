package com.homo.core.utils.origin.okhttp;

import java.io.Serializable;

public   class OkHttpClientConfig implements Serializable {

    private static final long serialVersionUID = 583390036889104243L;
    int maxIdleConnections=5;
    int keepAliveSeconds=3600;
    int readTimeOutSeconds=180;
    int writeTimeOutSeconds=180;
    int connectTimeoutSeconds=180;
    int maxRequests=64;
    int maxRequestsPerHost=64;

    public OkHttpClientConfig() {
    }

    public OkHttpClientConfig(int maxIdleConnections, int keepAliveSeconds, int readTimeOutSeconds, int writeTimeOutSeconds, int connectTimeoutSeconds, int maxRequests, int maxRequestsPerHost) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveSeconds = keepAliveSeconds;
        this.readTimeOutSeconds = readTimeOutSeconds;
        this.writeTimeOutSeconds = writeTimeOutSeconds;
        this.connectTimeoutSeconds = connectTimeoutSeconds;
        this.maxRequests = maxRequests;
        this.maxRequestsPerHost = maxRequestsPerHost;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public int getReadTimeOutSeconds() {
        return readTimeOutSeconds;
    }

    public void setReadTimeOutSeconds(int readTimeOutSeconds) {
        this.readTimeOutSeconds = readTimeOutSeconds;
    }

    public int getWriteTimeOutSeconds() {
        return writeTimeOutSeconds;
    }

    public void setWriteTimeOutSeconds(int writeTimeOutSeconds) {
        this.writeTimeOutSeconds = writeTimeOutSeconds;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
    }
}