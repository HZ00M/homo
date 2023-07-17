package com.homo.core.rpc.http;

import brave.Span;
import com.alibaba.fastjson.JSON;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.rpc.base.serial.ByteRpcContent;
import com.homo.core.rpc.base.serial.JsonRpcContent;
import com.homo.core.rpc.http.dto.ResponseMsg;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;

import java.nio.charset.StandardCharsets;

/**
 * RPC Http服务器实现
 * 用于处理http的RPC请求
 * 支持json,pb
 * json对象需要再消息头选择数据类型为application/json。RPC接受方会将消息作为一个jsonObj
 * 否则默认作为pb来处理
 *
 */
@Log4j2
public class HttpServer {
    private RpcServer rpcServer;
    private DisposableServer nettyHttpServer;
    public HttpServer(RpcServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    public void start(ApplicationContext applicationContext) {
        /**
         * 基于spring环境构建httpHandler
         */
        HttpHandler httpHandler = WebHttpHandlerBuilder.applicationContext(applicationContext).build();
        ReactorHttpHandlerAdapter handlerAdapter = new ReactorHttpHandlerAdapter(httpHandler);
        nettyHttpServer = reactor.netty.http.server.HttpServer.create().port(getPort())
                .protocol(HttpProtocol.HTTP11,HttpProtocol.H2C)//设置服务器支持的协议，默认的http1.1以及Http2 clear-text
                .wiretap(true)
                .handle(handlerAdapter).bind().block();
        log.info("HttpServer start at {}", rpcServer.getPort());
    }

    public void stop() {
        nettyHttpServer.disposeNow();
    }

    public Integer getPort() {
        return rpcServer.getPort();
    }

    /**
     * for proto
     * @param msgId
     * @param data
     * @param response
     * @return
     * @param <T>
     */
    public <T> Mono<DataBuffer> onCall(String msgId, byte[][] data,ServerHttpResponse response) {
        Span span = ZipkinUtil.currentSpan();
        ByteRpcContent rpcContent = new ByteRpcContent(data, span);
        return rpcServer.onCall("HttpServer",msgId,rpcContent)
                .nextDo(ret->{
                    byte[][] res = ret;
                    NettyDataBufferFactory dataBufferFactory = (NettyDataBufferFactory) response.bufferFactory();
                    DataBuffer buffer = dataBufferFactory.wrap(res[0]);
                    return Mono.just(buffer);
                });
    }

    /**
     * for jsonData
     * @param msgId
     * @param data
     * @param response
     * @return
     * @param <T>
     */
    public <T> Mono<DataBuffer> onCall(String msgId, String data,ServerHttpResponse response) {
        Span span = ZipkinUtil.currentSpan();
        JsonRpcContent rpcContent = new JsonRpcContent(data);
        return rpcServer.onCall("HttpServer",msgId,rpcContent)
                .nextDo(ret->{
                    ResponseMsg responseMsg = ResponseMsg.builder().msgId(msgId).msg("ok").msgContent(ret).code(HttpStatus.OK.value()).build();
                    String resStr = JSON.toJSONString(responseMsg);
                    log.info("onCall success msgId {} responseMsg {}", msgId, resStr);
                    NettyDataBufferFactory dataBufferFactory = (NettyDataBufferFactory) response.bufferFactory();
                    DataBuffer buffer = dataBufferFactory.wrap(resStr.getBytes(StandardCharsets.UTF_8));
                    return Mono.just(buffer);
                })
                .onErrorContinue(throwable -> {
                    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    log.info("onCall begin msgId {} throwable", msgId, throwable);
                    return Homo.error(throwable);
                })
                ;
    }

    /**
     * for upload file
     * @param msgId
     * @param rpcContent
     * @param response
     * @return
     */
    public Mono<DataBuffer> onFileUpload(String msgId, FileRpcContent rpcContent,ServerHttpResponse response )  {
        return rpcServer.onCall("HttpServer",msgId,rpcContent)
                .nextDo(ret->{
                    NettyDataBufferFactory dataBufferFactory = (NettyDataBufferFactory) response.bufferFactory();
                    DataBuffer buffer = dataBufferFactory.wrap("success".getBytes(StandardCharsets.UTF_8));
                    return Mono.just(buffer);
                });
    }
}
