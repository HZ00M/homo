package com.homo.core.rpc.http.filter;

import brave.Span;
import brave.Tracer;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Log4j2
public class TraceFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Span span = ZipkinUtil.newSRSpan()
                .tag(ZipkinUtil.SERVER_RECEIVE_TAG, "filter")
                .tag("url", exchange.getRequest().getURI().toASCIIString());
        Tracer.SpanInScope spanInScope = ZipkinUtil.startScope(span);
        return chain.filter(exchange)
                .doFinally(sing -> {
                    span
                            .tag(ZipkinUtil.FINISH_TAG, "TraceWebFilter.filter")
                            .annotate(ZipkinUtil.SERVER_SEND_TAG)
                            .finish();
                    spanInScope.close();
                });
    }
}
