package com.homo.core.rpc.http.filter;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import com.homo.core.rpc.http.HomoHttpHeader;
import com.homo.core.utils.trace.TraceLogUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public class TraceFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String url = exchange.getRequest().getURI().toASCIIString();
        Span rootSpan = buildRootSpan(url, exchange);
        long traceId = rootSpan.context().traceId();
        log.trace("TraceFilter process start url {} traceId {}", url, traceId);
        Tracer.SpanInScope spanInScope = ZipkinUtil.startScope(rootSpan);
        return chain.filter(exchange)
                .doFinally(sing -> {
                    rootSpan
                            .annotate(ZipkinUtil.SERVER_SEND_TAG);
                    log.trace("TraceFilter process end url {} traceId {}", url, traceId);
                    spanInScope.close();
                });
    }

    public static Span buildRootSpan(String name, ServerWebExchange exchange) {
        Span span = null;
        String url = exchange.getRequest().getURI().toASCIIString();
        Map<String, String> headerInfo = exchange.getRequest().getHeaders().toSingleValueMap();
        try {
            if (headerInfo.containsKey(HomoHttpHeader.X_TRACE_ID.param()) &&
                    headerInfo.containsKey(HomoHttpHeader.X_SPAN_ID.param()) &&
                    headerInfo.containsKey(HomoHttpHeader.X_SAMPLED.param())) {
                long traceId = Long.parseLong(headerInfo.get(HomoHttpHeader.X_TRACE_ID.param()));
                long spanId = Long.parseLong(headerInfo.get(HomoHttpHeader.X_SPAN_ID.param()));
                Boolean sampled = Boolean.parseBoolean(headerInfo.get(HomoHttpHeader.X_SAMPLED.param()));
                TraceContext traceContext =
                        TraceContext.newBuilder()
                                .spanId(spanId)
                                .traceId(traceId)
                                .sampled(sampled)
                                .build();
                span = ZipkinUtil.getTracing()
                        .tracer()
                        .newChild(traceContext)
                        .annotate(ZipkinUtil.SERVER_RECEIVE_TAG)
                        .name("nextProxyTrace")
                        .tag("url", url);
                log.info(
                        "DefaultMapping getSpan traceId {}, sampled {} parent_spanId {} spanId {} msgId {}",
                        Long.toHexString(traceContext.traceId()),
                        traceContext.sampled(),
                        Long.toHexString(traceContext.spanId()),
                        Long.toHexString(span.context().spanId()),
                        name);

            } else {
                span = ZipkinUtil.newSRSpan()
                        .name("proxyTrace")
                        .annotate(ZipkinUtil.SERVER_RECEIVE_TAG)
                        .tag("type", "router")
                        .tag("url", url);
            }
            TraceLogUtil.setTraceIdBySpan(span, name);
        } catch (Exception e) {
            log.error("DefaultMapping setSpanIfNeed msgId {} error", name, e);
        }

        return span;
    }
}
