package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import io.homo.proto.client.ParameterMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ByteRpcContent implements RpcContent<byte[][],byte[][]> {
    byte[][] data;
    Span span;


    @Override
    public RpcContentType getType() {
        return RpcContentType.BYTES;
    }

    @Override
    public byte[][] getData() {
        return data;
    }

    @Override
    public void setData(byte[][] data) {
        this.data = data;
    }

    @Override
    public Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId , ParameterMsg parameterMsg) {
        int paramCount = paramSerializeInfoList.length;
        if (paramCount <= 0) {
            return null;
        }
        Object[] returnParams = new Object[paramCount];
        if (frameParamOffset == 2){
            returnParams[0] = podId;
            returnParams[1] = parameterMsg;
        }
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
        if (paramSerializeInfoList == null || paramSerializeInfoList.length == 0) {
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


}
