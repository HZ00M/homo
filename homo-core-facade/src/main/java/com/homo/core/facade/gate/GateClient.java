package com.homo.core.facade.gate;


import com.homo.core.utils.callback.CallBack2;

/**
 * 客户端连接代理
 * 保持与客户端Channel连接
 */
public interface GateClient {
    /**
     * 消息回调函数
     * @param msgType 消息类型
     * @param data 消息数据
     */
    void onMsg(String msgType, byte[] data) throws Exception;

    /**
     * 消息回调函数
     * @param msgType 消息类型
     * @param data 消息数据
     * @param callBack 回调函数
     * @throws Exception
     */
    void onCallMsg(String msgType, byte[] data, CallBack2 callBack) throws Exception;

    /**
     * 连接打开回调函数
     */
    void onOpen();

    /**
     * 连接关闭回调函数
     * @param reason 关闭原因
     */
    void opClose(String reason);

    /**
     * 获取GateServer实例指针
     * @return GateServer实例指针
     */
    GateServer getGateServer();
}
