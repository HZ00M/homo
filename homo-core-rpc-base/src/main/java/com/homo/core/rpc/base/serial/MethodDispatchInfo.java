package com.homo.core.rpc.base.serial;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcContentType;
import com.homo.core.facade.rpc.SerializeInfo;
import com.homo.core.facade.security.RpcSecurity;
import com.homo.core.rpc.base.security.AccessControl;
import com.homo.core.utils.reflect.HomoTypeUtil;
import com.homo.core.utils.serial.FSTSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.JacksonSerializationProcessor;
import com.homo.core.utils.serial.ProtoSerializationProcessor;
import io.homo.proto.client.ParameterMsg;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiPredicate;

@Log4j2
@Data
public class MethodDispatchInfo implements RpcSecurity {
    private Method method;
    private int paramCount;
    private SerializeInfo[] paramSerializeInfos;
    private SerializeInfo[] returnSerializeInfos;
    private RpcSecurity rpcSecurity;
    private RpcContentType rpcContentType = RpcContentType.BYTES;
    // 向参数数组中填充podId和parameterMsg的标志
    private boolean paddingParams = false;
    private int paddingOffset;
    private final BiPredicate<Object, Object> needFillParamsPredicate = (o1, o2) -> {
        // 如果方法的参数列表第一个是Integer类型，第二个是是ParameterMsg，则设置填充参数标志为true
        return o1.getClass().isAssignableFrom(Integer.class) && o2.getClass().isAssignableFrom(ParameterMsg.class);
    };

    private MethodDispatchInfo(Method method) {
        this.method = method;
        this.init();
    }

    private void init() {
        this.paramCount = exportParamCount();
        this.paramSerializeInfos = exportSerializeInfos(method.getParameterTypes());
        this.returnSerializeInfos = exportSerializeInfos(getTypeParamsByType(method.getGenericReturnType()));
    }

    private SerializeInfo[] exportSerializeInfos(Class<?>[] classes) {
        if (classes == null) {
            return null;
        }
        try {
            SerializeInfo[] serializeInfos = new SerializeInfo[classes.length];
            for (int i = 0; i < classes.length; i++) {
                Class<?> clazz = classes[i];
                HomoSerializationProcessor serializationProcessor = null;
                if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                    serializationProcessor = new ProtoSerializationProcessor();
                } else if (JSONObject.class.isAssignableFrom(clazz) || JSONArray.class.isAssignableFrom(clazz)) {
                    serializationProcessor = new JacksonSerializationProcessor();
                    rpcContentType = RpcContentType.JSON;
                } else {
                    serializationProcessor = new FSTSerializationProcessor();
                }
                serializeInfos[i] = exportSerializeInfo(clazz, serializationProcessor);
            }
            if (serializeInfos.length > 1 && needFillParamsPredicate.test(serializeInfos[0].paramType, serializeInfos[1].paramType)) {
                paddingParams = true;
                paddingOffset = 2;
            }
            return serializeInfos;
        } catch (Exception e) {
            log.error("exportSerializeInfos error", e);
            return null;
        }
    }

    private SerializeInfo exportSerializeInfo(Class<?> clazz, HomoSerializationProcessor serializationProcessor) {
        SerializeInfo serializeInfo = SerializeInfo.create(clazz, serializationProcessor);
        return serializeInfo;
    }


    private Class<?>[] getTypeParamsByType(Type type) {
        Type[] genericTypes = HomoTypeUtil.getTypeArguments(type);
        Class<?>[] classes = new Class<?>[genericTypes.length];
        for (int i = 0; i < genericTypes.length; i++) {
            classes[i] = HomoTypeUtil.getClass(genericTypes[i]);
            if (!Serializable.class.isAssignableFrom(classes[i])) {
                return null;
            }
        }
        return classes;
    }

    private int exportParamCount() {
        return method.getParameterCount();
    }

    public static MethodDispatchInfo create(Method method) {
        return create(method, null);
    }

    public static MethodDispatchInfo create(Method method, RpcSecurity rpcSecurity) {
        MethodDispatchInfo methodDispatchInfo = new MethodDispatchInfo(method);
        if (rpcSecurity != null) {
            methodDispatchInfo.rpcSecurity = rpcSecurity;
        } else {
            methodDispatchInfo.rpcSecurity = AccessControl.create(method);
        }
        return methodDispatchInfo;
    }

    @Override
    public boolean isCallAllowed(String srcServiceName) {
        return rpcSecurity.isCallAllowed(srcServiceName);
    }


    public Object[] unSerializeParam(Integer podId, ParameterMsg parameterMsg, RpcContent rpcContent) {
        int paramCount = paramSerializeInfos.length;
        if (paramCount <= 0) {
            return null;
        }
        return rpcContent.unSerializeParams(paramSerializeInfos, paddingOffset, podId, parameterMsg);
    }

    public Object[] unSerializeParam(RpcContent rpcContent) {
        return this.unSerializeParam(null, null, rpcContent);
    }

    public RpcContent serializeParamContent(Object[] params) {
        if (paramSerializeInfos == null || paramSerializeInfos.length <= 0) {
            return new ByteRpcContent();
        }
        if (rpcContentType.equals(RpcContentType.BYTES)) {
            ByteRpcContent byteRpcContent = new ByteRpcContent();
            byte[][] bytesData = serializeParam(params, byteRpcContent);
            byteRpcContent.setData(bytesData);
            return byteRpcContent;
        } else if (rpcContentType.equals(RpcContentType.JSON)) {
            JsonRpcContent jsonRpcContent = new JsonRpcContent();
            String jsonData =  serializeParam(params, jsonRpcContent);
            jsonRpcContent.setData(jsonData);
            return jsonRpcContent;
        }
        return null;
    }

    public <T> T serializeParam(Object[] params, RpcContent<T> rpcContent) {
        T serializeData = null;
        if (paramSerializeInfos == null || paramSerializeInfos.length <= 0) {
            return serializeData;
        }
        if (rpcContentType.equals(RpcContentType.BYTES)) {
            serializeData = rpcContent.serializeParams(params, paramSerializeInfos, paddingOffset);
        } else if (rpcContentType.equals(RpcContentType.JSON)) {
            serializeData = rpcContent.serializeParams(params, paramSerializeInfos, paddingOffset);
        }
        return serializeData;
    }


    public Integer choicePodIndex(Object o, Method method, Object[] objects) {
        return null;
    }
}
