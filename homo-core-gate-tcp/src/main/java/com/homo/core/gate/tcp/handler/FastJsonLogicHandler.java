package com.homo.core.gate.tcp.handler;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.MessageType;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.serial.FastjsonSerializationProcessor;

import com.homo.core.utils.trace.ZipkinUtil;
import io.netty.channel.ChannelHandlerContext;

public abstract class FastJsonLogicHandler extends AbstractLogicHandler<JSONObject>{
    protected static FastjsonSerializationProcessor serializationProcessor = new FastjsonSerializationProcessor();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object source) throws Exception {
        GateMessagePackage messagePackage = (GateMessagePackage) source;
        if (messagePackage.getHeader().getType()== MessageType.JSON.ordinal()){
            JSONObject jsonObject = serializationProcessor.readValue(messagePackage.getBody(), JSONObject.class);
            GateClient gateClient = ctx.channel().attr(TcpGateDriver.clientKey).get();
            ZipkinUtil.startScope(ZipkinUtil.newSRSpan(), span -> process(jsonObject, gateClient), null);
        }else {
            //不是json数据 交给下一个handler处理
            ctx.fireChannelRead(source);
        }
    }
}
