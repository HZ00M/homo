package com.homo.core.utils.http;

import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpCallerFactory {
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * json body数据
     */
    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    /**
     * 数据被编码为key/value格式编码在Url后缀发送到服务器
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    /**
     * 表单key/value的post数据提交，以及需要在表单中进行文件上传时使用此格式
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    /**
     * 二进制流数据（如常见的文件下载）
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    /**
     * http1.1的sslContextSpec，忽略http11的证书校验
     */
    private final static SslProvider.ProtocolSslContextSpec HTTP11_SSL_CONTEXT_SPEC = Http11SslContextSpec.forClient()
            .configure(builder -> builder.trustManager(InsecureTrustManagerFactory.INSTANCE));

    /**
     * http2的sslContextSpec，忽略http2的证书校验
     */
    private final static SslProvider.ProtocolSslContextSpec HTTP2_SSL_CONTEXT_SPEC = Http2SslContextSpec.forClient()
            .configure(builder -> builder.trustManager(InsecureTrustManagerFactory.INSTANCE));
    /**
     * 最大的连接数。取CPU核心数的2倍。最大为16线程。
     * max运算防止CPU核心数在容器中有bug获取为0的情况
     */
    public static final int DEFAULT_MAX_CONNECTIONS = Math.min(Math.max(1, Runtime.getRuntime().availableProcessors()) * 2, 16);

    /**
     * the maximum number of registered requests for acquire to keep in the pending queue
     * 最大的注册请求数量
     */
    public static final int DEFAULT_PENDING_ACQUIRE_MAX_COUNT = 3000;
    /**
     * Set the options to use for configuring {@link ConnectionProvider} acquire timeout (resolution: ms).
     * 消息在队列中的最大等待时间
     */
    public static final int DEFAULT_PENDING_ACQUIRE_TIMEOUT = 45000;

    /**
     * Set the options to use for configuring {@link ConnectionProvider} max idle time (resolution: ms).
     * 连接的最大空闲时间
     */
    public static final int DEFAULT_MAX_IDLE_TIME_MILLIS = DEFAULT_PENDING_ACQUIRE_TIMEOUT * 2;

    /**
     * socket连接超时时间
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 5000;

    /**
     * response超时时间
     */
    public static final int DEFAULT_RESPONSE_TIMEOUT_MILLIS = 20000;
    Map<String, HttpClient> httpClientMap = new ConcurrentHashMap<>(32);

    public HttpClient getHttpClientCache(String host) {
        return httpClientMap.computeIfAbsent(host, s -> buildHttpClient(host, 1000, 3000, 10000));
    }

    public void putHttpClientCache(String host, HttpClient httpClient) {
        httpClientMap.put(host, httpClient);
    }

    public static HttpClient buildHttpClient(@NotNull String name) {
        return buildHttpClient(name, DEFAULT_PENDING_ACQUIRE_MAX_COUNT, DEFAULT_PENDING_ACQUIRE_TIMEOUT, DEFAULT_MAX_CONNECTIONS);
    }

    public static HttpClient buildHttpClient(@NotNull String name, int pendingAcquireMaxCount, int pendingAcquireTimeout, int maxConnections) {
        return buildHttpClient(name, pendingAcquireMaxCount, pendingAcquireTimeout, maxConnections, APPLICATION_JSON);
    }

    public static HttpClient buildHttpClient(@NotNull String name, int pendingAcquireMaxCount, int pendingAcquireTimeout, int maxConnections, @NotNull String contentType) {
        Map<String, Object> headers = new HashMap();
        headers.put("Content-Type", contentType);
        ConnectionProvider connectionProvider = createConnectionProvider(name, pendingAcquireMaxCount, pendingAcquireTimeout, maxConnections);
        return buildHttpClient(connectionProvider, (Map) headers);
    }

    public static HttpClient buildHttpClient(@NotNull ConnectionProvider connectionProvider, @Nullable Map<String, Object> headers) {
        DefaultHttpHeaders defaultHttpHeaders = new DefaultHttpHeaders();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(defaultHttpHeaders::set);
        }
        return buildHttpClient(connectionProvider, defaultHttpHeaders);
    }

    public static HttpClient buildHttpClient(@NotNull ConnectionProvider connectionProvider, @Nullable HttpHeaders headers) {
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .keepAlive(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT_MILLIS)
                .responseTimeout(Duration.ofMillis(DEFAULT_RESPONSE_TIMEOUT_MILLIS))
                .headers(h -> {
                    if (headers != null) {
                        h.set(headers);
                    }
                    //如果没有设置content-type，设置为默认的json
                    if (!h.contains(CONTENT_TYPE)) {
                        h.set(CONTENT_TYPE, APPLICATION_JSON);
                    }
                });
        //配置ssl信任管理器
        return secureSslEmptyTrust(httpClient, HttpProtocol.HTTP11);
    }

    /**
     * 根据协议设置protocol和sslContextSpec
     *
     * @param httpClient 客户端实例
     * @param protocol   配置不同协议的sslContextSpec
     * @return httpClient
     */
    public static HttpClient secureSslEmptyTrust(@NotNull HttpClient httpClient, @NotNull HttpProtocol protocol) {
        if (protocol == HttpProtocol.HTTP11) {
            return httpClient.protocol(protocol).secure(spec -> spec.sslContext(HTTP11_SSL_CONTEXT_SPEC));
        } else {
            return httpClient.protocol(protocol).secure(spec -> spec.sslContext(HTTP2_SSL_CONTEXT_SPEC));
        }
    }

    public static ConnectionProvider createConnectionProvider(@NotNull String name, int pendingAcquireMaxCount, int pendingAcquireTimeout, int maxConnections) {
        return createConnectionProvider(name, pendingAcquireMaxCount, pendingAcquireTimeout, maxConnections, DEFAULT_MAX_IDLE_TIME_MILLIS);
    }

    public static ConnectionProvider createConnectionProvider(@NotNull String name, int pendingAcquireMaxCount, int pendingAcquireTimeout, int maxConnections, int maxIdleTimeMillis) {
        return ConnectionProvider.builder(name).maxConnections(maxConnections)
                .pendingAcquireMaxCount(pendingAcquireMaxCount)
                .pendingAcquireTimeout(Duration.ofMillis(pendingAcquireTimeout))
                .maxIdleTime(Duration.ofMillis(maxIdleTimeMillis))
                .lifo()
                .build();
    }
}
