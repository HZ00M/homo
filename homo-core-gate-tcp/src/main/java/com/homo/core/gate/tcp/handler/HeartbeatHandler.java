package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateMessagePackage;
import com.homo.core.facade.gate.GateMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    private int heartbeatCount = 0;// 心跳计数器，如果一直接收到的是心跳消息，达到一定数量之后，说明客户端一直没有用户操作了，服务器就主动断开连接。
    private int maxHeartbeatCount = 66;// 最大心跳数

    /**
     * 事件处理函数
     *
     * @param ctx 触发事件的channel的context
     * @param evt 触发事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        log.debug("userEventTriggered address {}", ctx.channel().remoteAddress());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            IdleState state = idleStateEvent.state();
            //心跳超时
            switch (state) {
                case ALL_IDLE:
                case READER_IDLE:
                case WRITER_IDLE:
                    log.warn("idle state event {}, close the connect!", state);
//                    tcpGateDriver.closeConnection(ctx,"userEventTriggered idleStateEvent");
                    ctx.close();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketAddress address = ctx.channel().remoteAddress();
        GateMessagePackage messagePackage = (GateMessagePackage) msg;// 拦截心跳请求，并处理
        if (messagePackage.getHeader().getType() == GateMessageType.HEART_BEAT.ordinal()) {
            GateMessagePackage response = new GateMessagePackage(null);
            response.setType(GateMessageType.HEART_BEAT.ordinal());
            ctx.writeAndFlush(response);
            this.heartbeatCount++;
            if (heartbeatCount > maxHeartbeatCount) {
                log.info("channelRead heartbeatCount > maxHeartbeatCount, close the connect! address {}",address);
                ctx.close();
            }
        } else {
            this.heartbeatCount = 0;
            log.info("channelRead address {} header {}", address,messagePackage.getHeader());
            ctx.fireChannelRead(msg);
        }
    }
}
