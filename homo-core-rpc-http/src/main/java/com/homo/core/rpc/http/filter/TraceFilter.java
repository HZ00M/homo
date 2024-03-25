package com.homo.core.rpc.http.filter;

import brave.Span;
import brave.Tracer;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class TraceFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String url = exchange.getRequest().getURI().toASCIIString();
        Span rootSpan = ZipkinUtil.newSRSpan()
                .name("proxyTrace")
                .annotate(ZipkinUtil.SERVER_RECEIVE_TAG)
                .tag("type", "router")
                .tag("url", url);
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
}
