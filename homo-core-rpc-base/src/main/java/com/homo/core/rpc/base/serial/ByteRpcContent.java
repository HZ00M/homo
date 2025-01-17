package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import io.homo.proto.client.ParameterMsg;
import lombok.Data;

@Data
public class ByteRpcContent implements RpcContent<byte[][],byte[]> {
    private String msgId;
    private byte[][] paramData;
    private byte[] returnData;
    private Class<?> returnType;
    Span span;

    @Override
    public String getId() {
        return msgId;
    }
    @Override
    public void setId(String id) {
        this.msgId = id;
    }


    @Override
    public RpcContentType getType() {
        return RpcContentType.BYTES;
    }

    @Override
    public byte[][] getParam() {
        return paramData;
    }

    @Override
    public void setParam(byte[][] data) {
        this.paramData = data;
    }

    @Override
    public byte[] getReturn() {
        return returnData;
    }

    @Override
    public void setReturn(byte[] data) {
        this.returnData = data;
    }

    @Override
    public Object[] unSerializeToActualParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId , ParameterMsg parameterMsg) {
        int paramCount = paramSerializeInfoList.length;
        if (paramCount <= 0) {
            return null;
        }
        Object[] actualParam = new Object[paramCount];
        if (frameParamOffset == 2){
            actualParam[0] = podId;
            actualParam[1] = parameterMsg;
        }
        byte[][] data = getParam();
        int dataIndex = 0;
        for (int i = frameParamOffset; i < paramSerializeInfoList.length; i++) {
            Object value = paramSerializeInfoList[i].processor.readValue(data[dataIndex], paramSerializeInfoList[i].paramType);
            actualParam[i] = value;
            dataIndex++;
        }
        return actualParam;
    }

    @Override
    public byte[][] serializeRawParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        if (paramSerializeInfoList == null || paramSerializeInfoList.length == 0) {
            return null;
        }
        byte[][] byteParams = new byte[paramSerializeInfoList.length][];
        int dataIndex = 0;
        for (int i = frameParamOffset; i < paramSerializeInfoList.length; i++) {
            Object obj = params[dataIndex];
            byteParams[i] = paramSerializeInfoList[i].processor.writeByte(obj);
            dataIndex ++;
        }
        return byteParams;
    }

    @Override
    public byte[] serializeReturn(Object returnValue, SerializeInfo returnSerializeInfo) {
        byte[] returnBytes = returnSerializeInfo.processor.writeByte(returnValue);
        return returnBytes;
    }

    @Override
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

}
