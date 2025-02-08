package com.homo.core.rpc.http;

import brave.Span;
import brave.Tracer;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.serial.FileRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
import com.homo.core.utils.upload.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;

@Slf4j
public class HttpRpcAgentClient implements RpcAgentClient {
    private final WebClient webClient;
    private final String srcServiceName;
    private final String targetServiceName;
    private final Integer targetPort;
    private final boolean srcIsStateful;
    private final boolean targetIsStateful;

    public HttpRpcAgentClient(String srcServiceName, String targetServiceName, Integer targetPort, WebClient webClient, boolean srcIsStateful, boolean targetIsStateful) {
        this.webClient = webClient;
        this.srcServiceName = srcServiceName;
        this.targetServiceName = targetServiceName;
        this.targetPort = targetPort;
        this.srcIsStateful = srcIsStateful;
        this.targetIsStateful = targetIsStateful;
    }

    @Override
    public Homo rpcCall(String funName, RpcContent content) {
        Span span = ZipkinUtil.getTracing().tracer()
                .nextSpan()
                .name(funName)
                .tag("type", "rpcCall")
                .tag("srcServiceName", srcServiceName)
                .tag("targetServiceName", targetServiceName)
                .tag("funName", funName);
        try (Tracer.SpanInScope spanInScope = ZipkinUtil.getTracing().tracer().withSpanInScope(span)) {
            Homo rpcResult;
            if (content.getType().equals(RpcContentType.BYTES)) {
                ByteRpcContent byteRpcContent = (ByteRpcContent) content;
                byte[][] data = byteRpcContent.getParam();
                rpcResult = httpProtoHandle(funName, data)
                        .consumerValue(ret->{
                            byteRpcContent.setReturn(ret);
                        });
            } else if (content.getType().equals(RpcContentType.JSON)) {
                JsonRpcContent jsonRpcContent = (JsonRpcContent) content;
                String data = jsonRpcContent.getParam();
                rpcResult = httpJsonHandle(funName, data)
                        .consumerValue(ret->{
                            jsonRpcContent.setReturn(ret);
                        });
            } else if (content.getType().equals(RpcContentType.FILE)) {
                FileRpcContent fileRpcContent = (FileRpcContent) content;
                UploadFile uploadFile = fileRpcContent.getParam();
                rpcResult = httpFormHandle(funName, uploadFile)
                        .consumerValue(ret->{
                            fileRpcContent.setReturn(ret);
                        });
            } else {
                log.error("rpcCall contentType unknown, targetServiceName {} funName {} contentType {}", targetServiceName, funName, content.getType());
                rpcResult = Homo.error(new RuntimeException("rpcCall contentType unknown"));
            }
            return rpcResult.consumerValue(ret -> {
                span.finish();
            });
        } catch (Exception e) {
            span.error(e);
            return Homo.error(e);
        }
    }

    private Homo<String> httpJsonHandle(String funName, String param) {
        Span span = ZipkinUtil.currentSpan();
        String uri = URI.create(String.format("http://%s:%d/%s", targetServiceName, targetPort, funName)).toString();
        try {
            return Homo.warp(
                    webClient.post()
                            .uri(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HomoHttpHeader.X_TRACE_ID.param(), String.valueOf(span.context().traceId()))
                            .header(HomoHttpHeader.X_SPAN_ID.param(), String.valueOf(span.context().spanId()))
                            .header(HomoHttpHeader.X_SAMPLED.param(), String.valueOf(span.context().sampled()))
                            .bodyValue(param)
                            .retrieve()
                            .bodyToMono((String.class))
//                            .bodyToMono((byte[].class))
                            .doOnSuccess(response -> span.finish())
                            .doOnError(span::error)// 阻塞调用，可改为异步处理。
            );

        } catch (WebClientResponseException e) {
            log.error("HTTP request failed: {}", e.getResponseBodyAsString(), e);
            return Homo.error(e);
        }
    }

    private Homo<byte[]> httpProtoHandle(String funName, byte[][] data) {
        Span span = ZipkinUtil.currentSpan();
        String uri = URI.create(String.format("http://%s:%d/%s", targetServiceName, targetPort, funName)).toString();
        try {
            return Homo.warp(
                    webClient.post()
                            .uri(uri)
                            .header(HttpHeaders.CONTENT_TYPE,MediaType.ALL_VALUE)
                            .header(HomoHttpHeader.X_TRACE_ID.param(), String.valueOf(span.context().traceId()))
                            .header(HomoHttpHeader.X_SPAN_ID.param(), String.valueOf(span.context().spanId()))
                            .header(HomoHttpHeader.X_SAMPLED.param(), String.valueOf(span.context().sampled()))
                            .bodyValue(data[0])//headerInfo为第二个参数不传 目前仅支持传byte[]
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .doOnSuccess(response -> span.finish())
                            .doOnError(span::error)
            ); // 阻塞调用，可改为异步处理。)
        } catch (WebClientResponseException e) {
            log.error("HTTP request failed: {}", e.getResponseBodyAsString(), e);
            return Homo.error(e);
        }
    }

    private Homo<byte[]> httpFormHandle(String funName, UploadFile uploadFile) {
        Span span = ZipkinUtil.currentSpan();
        String uri = URI.create(String.format("http://%s:%d/%s", targetServiceName, targetPort, funName)).toString();
        try {
            return Homo.warp(
                    webClient.post()
                            .uri(uri)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header(HomoHttpHeader.X_TRACE_ID.param(), String.valueOf(span.context().traceId()))
                            .header(HomoHttpHeader.X_SPAN_ID.param(), String.valueOf(span.context().spanId()))
                            .header(HomoHttpHeader.X_SAMPLED.param(), String.valueOf(span.context().sampled()))
                            .bodyValue(uploadFile.content())
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .doOnSuccess(response -> span.finish())
                            .doOnError(span::error)
            );
        } catch (WebClientResponseException e) {
            log.error("HTTP request failed: {}", e.getResponseBodyAsString(), e);
            return Homo.error(e);
        }
    }
}
