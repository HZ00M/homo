package com.homo.core.rpc.http.exception;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Log4j2
public class HomoHttpExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NotNull Throwable throwable) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        log.error("exchange {} error",request.getId(),throwable);
        NettyDataBufferFactory dataBufferFactory =
                (NettyDataBufferFactory) response.bufferFactory();
        DataBuffer buffer = dataBufferFactory.wrap("error".getBytes(StandardCharsets.UTF_8));
        Mono<Mono<DataBuffer>> resp = Mono.just(Mono.just(buffer));
        return response.writeAndFlushWith(resp);
    }
}
