package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import io.homo.proto.client.ParameterMsg;
import lombok.Data;

public class ByteRpcContent implements RpcContent<byte[][], byte[]> {
    private String msgId;
    private byte[][] paramData;
    private byte[] returnData;
    private Class<?> returnType;
    Span span;

    @Override
    public String getMsgId() {
        return msgId;
    }

    @Override
    public void setMsgId(String id) {
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
    public void setReturn(byte[] returnData) {
        this.returnData = returnData;
    }

    @Override
    public byte[] getReturn() {
        return returnData;
    }

    @Override
    public Object[] unSerializeToActualParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId, ParameterMsg parameterMsg) {
        int paramCount = paramSerializeInfoList.length;
        if (paramCount <= 0) {
            return null;
        }
        Object[] actualParam = new Object[paramCount];
        if (frameParamOffset == 2 && podId != null && parameterMsg != null) {
            //有传填充参数使用填充的参数
            actualParam[0] = podId;
            actualParam[1] = parameterMsg;
        }else {
            frameParamOffset = 0;
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
        if (params.length >= 2) {
            if (params[0] instanceof Integer && params[1] instanceof ParameterMsg){
                //rpc client直接传填充参数时，不进行参数偏移
                frameParamOffset = 0;
            }
        }
        for (int i = frameParamOffset; i < paramSerializeInfoList.length; i++) {
            Object obj = params[dataIndex];
            byteParams[i] = paramSerializeInfoList[i].processor.writeByte(obj);
            dataIndex++;
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

    @Override
    public Span getSpan() {
        return span;
    }

    @Override
    public void setSpan(Span span) {
        this.span = span;
    }

    @Override
    public Object unSerializeReturnValue(SerializeInfo returnSerializeInfo) {
        return returnSerializeInfo.processor.readValue(returnData, returnSerializeInfo.paramType);
    }

}
