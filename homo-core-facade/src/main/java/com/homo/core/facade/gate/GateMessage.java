package com.homo.core.facade.gate;

import lombok.Data;

public interface GateMessage<T> {

    Header getHeader();

    byte[] serial();

    byte[] getBody();

    static GateMessage makeMessage(Header header,byte[] body){
        return new GateMessage() {
            @Override
            public Header getHeader() {
                return header;
            }

            @Override
            public byte[] getBody() {
                return body;
            }

            @Override
            public byte[] serial() {
                return body;
            }//todo 暂不使用
        };
    }

    /**
     * 头结构: |bodySize(4B)|version(1B)|packType(1B)|opTime(8B)|sessionId(2B)|opSeq(2B)
     * todo 数据压缩支持
     */
    int HEAD_LENGTH = 18;
    @Data
    class Header implements Cloneable {

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
        private short sendSeq = -DEFAULT_SEND_SEQ;
        //接受序号
        private short recvSeq = DEFAULT_RECV_SEQ;

    }
    short DEFAULT_SEND_SEQ = -1;
    short DEFAULT_RECV_SEQ = -1;
//    @Data
//    class Header implements Cloneable {
//
//        //body大小
//        private int bodySize;
//        //版本号
//        private int version;
//        //消息类型
//        private MessageType type;
//        //客户端发送时间
//        private long clientSendTime;
//        //会话id
//        private short sessionId;
//        //发送序号
//        private short sendSeq = Short.MAX_VALUE;
//        //接受序号
//        private short recvSeq = Short.MAX_VALUE;
//        //服务器发送时间
//        private long serverSendTime;
//    }


}