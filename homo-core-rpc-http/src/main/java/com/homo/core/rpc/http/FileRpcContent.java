package com.homo.core.rpc.http;

import brave.Span;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import com.homo.core.rpc.http.upload.UploadFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRpcContent implements RpcContent<UploadFile> {
    UploadFile data;
    RpcContentType type;
    Span span;


    @Override
    public RpcContentType getType() {
        return type;
    }

    @Override
    public UploadFile getData() {
        return data;
    }

    @Override
    public void setData(UploadFile data) {
        this.data = data;
    }

    @Override
    public Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        Object[] objects = new Object[1];
        objects[0] = getData();
        return objects;
    }

    @Override
    public byte[][] serializeParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        return null;
    }


    public void setType(RpcContentType type) {
        this.type = type;
    }
}
