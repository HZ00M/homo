package com.homo.core.facade.gate;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class GateMessageHeader implements Cloneable {
    public static short DEFAULT_SEND_SEQ = -1;
    public static short DEFAULT_RECV_SEQ = -1;
    //body大小
    private int bodySize;
    //版本号
    private int version;
    //消息类型 目前分为proto json
    private int type;
    // 操作时间
    private long opTime;
    //会话id 一个请求与一个响应对应
    private short sessionId;
    //发送序号 客户端与服务器自增的发送序号，序号为0表示心跳包做特殊处理，可以用该序号做去重处理
    private short sendSeq = DEFAULT_SEND_SEQ;
    //接受序号 接收确认序号
    private short recvSeq = DEFAULT_RECV_SEQ;

}
