package com.homo.core.gate.tcp.handler;

import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.trace.ZipkinUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 入站尾处理器，处理连接相关操作
 * @param <T>
 */
@Log4j2
public class TailHandler<T> extends ChannelInboundHandlerAdapter {
    private final TcpGateDriver<T> tcpGateDriver;

    public TailHandler(TcpGateDriver<T> tcpGateDriver){
        this.tcpGateDriver = tcpGateDriver;
        tcpGateDriver.registerAfterHandler(this);//将节点注册到后处理器
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
            //心跳超时
            log.warn("idle state event, close the connect!");
            tcpGateDriver.closeConnection(ctx,"userEventTriggered idleStateEvent");
            ctx.close();
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
