package com.homo.core.rpc.http.exception;

import com.alibaba.fastjson.JSON;

import com.homo.core.rpc.http.dto.ResponseMsg;
import com.homo.core.utils.exception.HomoException;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * webFlux全局异常处理
 */
@Log4j2
public class HomoHttpExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, @NotNull Throwable throwable) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        log.error("HomoHttpExceptionHandler exchange {} error", request.getId(), throwable);
        ResponseMsg msg;
        if (throwable instanceof HomoException){
            HomoException homoException = (HomoException) throwable;
            msg = ResponseMsg.builder().codeDesc(homoException.getMessage()).code(homoException.getCode()).build();
        }else {
            msg = ResponseMsg.builder().codeDesc(throwable.getMessage()).code(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        }
        String repStr = JSON.toJSONString(msg);

        NettyDataBufferFactory dataBufferFactory =
                (NettyDataBufferFactory) response.bufferFactory();
        DataBuffer buffer = dataBufferFactory.wrap(repStr.getBytes(StandardCharsets.UTF_8));
        Mono<Mono<DataBuffer>> resp = Mono.just(Mono.just(buffer));
        return response.writeAndFlushWith(resp);
    }
}
