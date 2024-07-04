package com.homo.core.facade.mq;

/**
 * 消息编解码接口
 */
public interface MQCodeC<SRC,DEST> {
    /**
     * 编码
     * @param src
     * @return
     * @throws Exception
     */
    DEST encode(SRC src) throws Exception;

    /**
     * 解码
     * @param dest
     * @return
     * @throws Exception
     */
    SRC decode(DEST dest) throws Exception;
}
