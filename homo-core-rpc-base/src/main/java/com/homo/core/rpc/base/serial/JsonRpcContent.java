package com.homo.core.rpc.base.serial;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
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
public class JsonRpcContent implements RpcContent<String> {
    private String data;

    @Override
    public RpcContentType getType() {
        return RpcContentType.JSON;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public Object[] unSerializeParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId, ParameterMsg parameterMsg) {
        int paramCount = paramSerializeInfoList.length;
        if (paramCount <= 0) {
            return null;
        }
        Object[] returnParams = new Object[paramCount];
        if (frameParamOffset == 2) {
            returnParams[0] = podId;
            returnParams[1] = parameterMsg;
        }
        String jsonStr = getData();
        JSONValidator.Type type = JSONValidator.from(jsonStr).setSupportMultiValue(true).getType();
        if (type == JSONValidator.Type.Array) {
            JSONArray jsonArray = JSON.parseArray(jsonStr);
            for (int i = 0; i < paramSerializeInfoList.length; i++) {
                Object obj = jsonArray.get(i);
                if (obj instanceof JSONObject) {
                    obj = ((JSONObject) obj).toJavaObject(paramSerializeInfoList[i + frameParamOffset].getParamType());
                    returnParams[i + frameParamOffset] = obj;
                }
            }
        } else if (type == JSONValidator.Type.Object) {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            Object object = jsonObject.toJavaObject(paramSerializeInfoList[frameParamOffset].getParamType());
            returnParams[frameParamOffset] = object;
        }else {
            returnParams[frameParamOffset] = jsonStr;
        }
        return returnParams;
    }

    @Override
    public String serializeParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        String retStr;
        if (params.length == 1) {
            if (params[0] instanceof String) {
                retStr = (String) params[0];
            } else {
                retStr = JSON.toJSONString(params[0]);
            }
        } else {
            retStr = JSON.toJSONString(params);
        }
        return retStr;
    }
}
