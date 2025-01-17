package com.homo.core.rpc.base.serial;

import brave.Span;
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

public class JsonRpcContent implements RpcContent<String, String> {
    private String msgId;
    private String paramData;
    private String returnData;
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
        return RpcContentType.JSON;
    }

    @Override
    public String getParam() {
        return paramData;
    }

    @Override
    public void setParam(String data) {
        this.paramData = data;
    }

    @Override
    public String getReturn() {
        return returnData;
    }

    @Override
    public void setReturn(String data) {
        this.returnData = data;
    }

    @Override
    public Object[] unSerializeToActualParams(SerializeInfo[] paramSerializeInfoList, int frameParamOffset, Integer podId, ParameterMsg parameterMsg) {
        int paramCount = paramSerializeInfoList.length;
        if (paramCount <= 0) {
            return null;
        }
        Object[] returnParams = new Object[paramCount];
        if (frameParamOffset == 2) {
            returnParams[0] = podId;
            returnParams[1] = parameterMsg;
        }
        String jsonStr = getParam();
        JSONValidator.Type type = JSONValidator.from(jsonStr).setSupportMultiValue(true).getType();
        if (type == JSONValidator.Type.Array) {
            JSONArray jsonArray = JSON.parseArray(jsonStr);
            for (int i = 0; i < paramSerializeInfoList.length - frameParamOffset; i++) {
                int paramIndex = i + frameParamOffset;
                Class<?> paramType = paramSerializeInfoList[paramIndex].getParamType();
                Object arrItem = jsonArray.get(i);
                Object obj = null;
                if (arrItem != null && arrItem.getClass().isAssignableFrom(paramType)) {
                    obj = arrItem;
                } else if (arrItem instanceof JSONObject) {
                    obj = ((JSONObject) arrItem).toJavaObject(paramType);
                } else if (arrItem instanceof String) {
                    String arrItemStr = (String) arrItem;
                    JSONValidator.Type itemJsonType = JSONValidator.from(arrItemStr).setSupportMultiValue(true).getType();
                    if (itemJsonType == JSONValidator.Type.Object && JSONObject.class.isAssignableFrom(paramType)) {
                        obj = JSON.parseObject(arrItemStr, paramType);
                    } else if (itemJsonType == JSONValidator.Type.Array && JSONArray.class.isAssignableFrom(paramType)) {
                        obj = JSON.parseArray(arrItemStr, paramType);
                    }else if (itemJsonType == JSONValidator.Type.Value && String.class.isAssignableFrom(paramType)) {
                        obj = arrItemStr;
                    }
                }
                returnParams[paramIndex] = obj;
            }
        } else if (type == JSONValidator.Type.Object) {
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            Object obj;
            Class<?> paramType = paramSerializeInfoList[frameParamOffset].getParamType();
            if (jsonObject.getClass().isAssignableFrom(paramType)) {
                obj = jsonObject;
            } else {
                obj = jsonObject.toJavaObject(paramType);
            }
            returnParams[frameParamOffset] = obj;
        } else {
            Object obj = null;
            if (jsonStr.getClass().isAssignableFrom(paramSerializeInfoList[frameParamOffset].getParamType())) {
                obj = jsonStr;
            }
            returnParams[frameParamOffset] = obj;
        }
        return returnParams;
    }

    @Override
    public String serializeRawParams(Object[] params, SerializeInfo[] paramSerializeInfoList, int frameParamOffset) {
        String retStr;
        //todo 这里需要优化  不再只序列化第一个参数
        if (params.length == 1) {
            if (params[0] instanceof String) {
                retStr = (String) params[0];
            } else {
                retStr = paramSerializeInfoList[0].getProcessor().writeString(params[0]);
            }
        } else {
            retStr = paramSerializeInfoList[0].getProcessor().writeString(params[0]);
        }
        return retStr;
    }

    @Override
    public String serializeReturn(Object returnValue, SerializeInfo returnSerializeInfo) {
        String retStr = returnSerializeInfo.getProcessor().writeString(returnValue);
        return retStr;
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
}
