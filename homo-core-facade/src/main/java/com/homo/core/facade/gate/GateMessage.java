package com.homo.core.facade.gate;

public interface GateMessage<T> {

    GateMessageHeader getHeader();

    byte[] serial();

    byte[] getBody();

    static GateMessage makeMessage(GateMessageHeader header,byte[] body){
        return new GateMessage() {
            @Override
            public GateMessageHeader getHeader() {
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
     * 头结构: |bodySize(4B)|version(1B)|packType(1B)|opTime(8B)|sessionId(2B)|sendSeq(2B)|recvReq(2B)
     * todo 数据压缩支持
     */
    int HEAD_LENGTH = 20;
//    @Data
//    public class Header implements Cloneable {
//
//        //body大小
//        private int bodySize;
//        //版本号
//        private int version;
//        //消息类型
//        private int type;
//        //操作时间
//        private long opTime;
//        //会话id
//        private short sessionId;
//        //发送序号
//        private short sendSeq = DEFAULT_SEND_SEQ;
//        //接受序号
//        private short recvSeq = DEFAULT_RECV_SEQ;

//    }



}