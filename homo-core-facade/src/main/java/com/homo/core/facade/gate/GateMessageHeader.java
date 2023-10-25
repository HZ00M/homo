package com.homo.core.facade.gate;

import lombok.Data;

@Data
public class GateMessageHeader implements Cloneable {
    public static short DEFAULT_SEND_SEQ = -1;
    public static short DEFAULT_RECV_SEQ = -1;
    //body大小
    private int bodySize;
    //版本号
    private int version;
    //消息类型
    private int type;
    //操作时间
    private long opTime;
    //会话id
    private short sessionId;
    //发送序号
    private short sendSeq = DEFAULT_SEND_SEQ;
    //接受序号
    private short recvSeq = DEFAULT_RECV_SEQ;

}
