package com.homo.core.gate.tcp.handler;

import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.trace.ZipkinUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 入站尾处理器，处理连接相关操作
 * @param
 */
@Log4j2
@ChannelHandler.Sharable
public class TailHandler extends ChannelInboundHandlerAdapter {
    private final TcpGateDriver tcpGateDriver;

    public TailHandler(TcpGateDriver tcpGateDriver){
        this.tcpGateDriver = tcpGateDriver;
    }

    /**
     * 处理新连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String hostAddress = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();
        ZipkinUtil.startScope(ZipkinUtil.newSRSpan().name("channelActive"), span -> {
            ctx.channel().attr(TcpGateDriver.sessionIdKey).setIfAbsent(Short.MIN_VALUE);//todo 暂时使用默认值
            short initSeq = 0;
            ctx.channel().attr(TcpGateDriver.serverSeqKey).setIfAbsent(initSeq);
            tcpGateDriver.createConnection(ctx,hostAddress,port);
        }, null);
    }

    /**
     * 处理连接断开
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive channelId {}",ctx.channel().id());
        ZipkinUtil.startScope(ZipkinUtil.newSRSpan().name("channelInactive"), span -> {
            tcpGateDriver.closeConnection(ctx,"channel inactive");
        }, null);
    }

    /**
     * 事件处理函数
     * @param ctx 触发事件的channel的context
     * @param evt 触发事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt){
        log.debug("on userEventTriggered");
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            IdleState state = idleStateEvent.state();
            //心跳超时
            switch (state){
                case ALL_IDLE:
                case READER_IDLE:
                case WRITER_IDLE:
                    log.warn("idle state event {}, close the connect!",state);
//                    tcpGateDriver.closeConnection(ctx,"userEventTriggered idleStateEvent");
                    ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException){
            log.info("exceptionCaught error channelId {} cause {}",ctx.channel().id().asLongText(), cause.getMessage());
        }else{
            log.error("exceptionCaught error ", cause);
        }
        ctx.close();
    }
}
