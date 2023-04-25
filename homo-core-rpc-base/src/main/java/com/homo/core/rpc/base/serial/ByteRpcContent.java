package com.homo.core.rpc.base.serial;

import brave.Span;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ByteRpcContent implements RpcContent<byte[][]> {
    byte[][] data;
    RpcContentType type;
    Span span;


    @Override
    public RpcContentType getType() {
        return type;
    }

    @Override
    public byte[][] getData() {
        return data;
    }

    @Override
    public void setData(byte[][] data) {

    }

    @Override
    public Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        int paramCount = paramSerializeInfoList.length;
        if (paramCount <= 0) {
            return null;
        }
        Object[] returnParams = new Object[paramCount];
        byte[][] data = getData();
        int dataIndex = 0;
        for (int i = frameParamOffset; i < paramSerializeInfoList.length; i++) {
            Object value = paramSerializeInfoList[i].processor.readValue(data[dataIndex], paramSerializeInfoList[i].paramType);
            returnParams[i] = value;
            dataIndex++;
        }
        return returnParams;
    }

    @Override
    public byte[][] serializeParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        if (paramSerializeInfoList == null || paramSerializeInfoList.length <= 0) {
            return null;
        }
        byte[][] byteParams = new byte[paramSerializeInfoList.length][];
        int dataIndex = 0;
        for (int i = frameParamOffset; i < paramSerializeInfoList.length; i++) {
            Object obj = params[dataIndex];
            byteParams[i] = paramSerializeInfoList[i].processor.writeByte(obj);
        }
        return byteParams;
    }


    public void setType(RpcContentType type) {
        this.type = type;
    }
}
