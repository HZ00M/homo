package com.homo.core.gate.tcp.handler;

import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.trace.ZipkinUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 入站尾处理器，处理连接相关操作
 *
 * @param
 */
@Slf4j
@ChannelHandler.Sharable
public class TailHandler extends ChannelInboundHandlerAdapter {
    private final TcpGateDriver tcpGateDriver;

    public TailHandler(TcpGateDriver tcpGateDriver) {
        this.tcpGateDriver = tcpGateDriver;
    }

    /**
     * 处理新连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String hostAddress = socketAddress.getAddress().getHostAddress();
        int port = socketAddress.getPort();
        log.info("channelActive channelId {} socketAddress {} hostAddress {} port {}",
                ctx.channel().id(), socketAddress, hostAddress, port);
        ZipkinUtil.startScope(ZipkinUtil.newSRSpan().name("channelActive"), span -> {
            tcpGateDriver.createConnection(ctx, hostAddress, port);
        }, null);
    }

    /**
     * 处理连接断开
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive address {}", ctx.channel().remoteAddress());
        ZipkinUtil.startScope(ZipkinUtil.newSRSpan().name("channelInactive"), span -> {
            tcpGateDriver.closeConnection(ctx, "channel inactive");
        }, null);
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.info("exceptionCaught error address {} cause {}", ctx.channel().remoteAddress(), cause.getMessage());
        } else {
            log.error("exceptionCaught error address {} e", ctx.channel().remoteAddress(), cause);
        }
        ctx.close();
    }
}
