package com.homo.core.rpc.base.serial;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import com.homo.core.utils.upload.UploadFile;
import io.homo.proto.client.ParameterMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRpcContent implements RpcContent<UploadFile,byte[]> {
    private String msgId;
    private UploadFile fileData;
    private byte[] returnData;
    private Span span;
    private Class<?> returnType;

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
        return RpcContentType.FILE;
    }

    @Override
    public UploadFile getParam() {
        return fileData;
    }

    @Override
    public void setParam(UploadFile data) {
        this.fileData = data;
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
        Object[] objects = new Object[1];
        objects[0] = getParam();
        return objects;
    }

    @Override
    public UploadFile serializeRawParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        return null;
    }

    @Override
    public byte[] serializeReturn(Object param, SerializeInfo returnSerializeInfo) {
        byte[] retBytes = returnSerializeInfo.getProcessor().writeByte(param);
        return retBytes;
    }

    @Override
    public Object unSerializeReturnValue(SerializeInfo returnSerializeInfo) {
        return returnSerializeInfo.processor.readValue(returnData,returnSerializeInfo.paramType);
    }
}
