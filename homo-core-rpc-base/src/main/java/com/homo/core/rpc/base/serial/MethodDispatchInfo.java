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
import com.homo.core.utils.serial.FastjsonSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.ProtoSerializationProcessor;
import com.homo.core.utils.upload.UploadFile;
import io.homo.proto.client.ParameterMsg;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiPredicate;

@Slf4j
@Data
public class MethodDispatchInfo implements RpcSecurity {
    private Method method;
    private int paramCount;
    private SerializeInfo[] paramSerializeInfos;
    private SerializeInfo returnSerializeInfo;
    private RpcSecurity rpcSecurity;
    private RpcContentType paramContentType = RpcContentType.BYTES;
    private RpcContent paramContent;
    private RpcContent returnContent;
    // 向参数数组中填充podId和parameterMsg的标志
    private boolean paddingParams = false;
    private int paddingOffset;
    private final BiPredicate<Class<?>, Class<?>> needFillParamsPredicate = (o1, o2) -> {
        // 如果方法的参数列表第一个是Integer类型，第二个是是ParameterMsg，则设置填充参数标志为true
        return o1.isAssignableFrom(Integer.class) && o2.isAssignableFrom(ParameterMsg.class);
    };

    private MethodDispatchInfo(Method method) {
        this.method = method;
        this.init();
    }

    private void init() {
        this.paramCount = exportParamCount();
        this.paramSerializeInfos = exportParamsSerializeInfo(method.getParameterTypes());
        this.returnSerializeInfo = exportrReturnSerializeInfo(method.getGenericReturnType());
    }

    private SerializeInfo exportrReturnSerializeInfo(Type returnType) {
        Class<?> generateClass = getTypeParamsByType(returnType)[0];
        if (returnType == null) {
            return null;
        }
        try {
            HomoSerializationProcessor serializationProcessor;
            if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(generateClass)) {
                serializationProcessor = new ProtoSerializationProcessor();
                returnContent = new ByteRpcContent();
            } else if (JSONObject.class.isAssignableFrom(generateClass) || JSONArray.class.isAssignableFrom(generateClass)) {
                serializationProcessor = new FastjsonSerializationProcessor();
                returnContent = new JsonRpcContent();
            } else if (UploadFile.class.isAssignableFrom(generateClass)) {
                serializationProcessor = null;//todo
                returnContent = new FileRpcContent();
            } else {
                serializationProcessor = new FSTSerializationProcessor();
                returnContent = new ByteRpcContent();
            }
            SerializeInfo serializeInfo = exportSerializeInfo(generateClass, serializationProcessor);
            return serializeInfo;
        } catch (Exception e) {
            log.error("exportrReturnSerializeInfo error", e);
            return null;
        }
    }

    private SerializeInfo[] exportParamsSerializeInfo(Class<?>[] classes) {
        if (classes == null) {
            return null;
        }
        try {
            SerializeInfo[] serializeInfos = new SerializeInfo[classes.length];
            for (int i = 0; i < classes.length; i++) {
                Class<?> clazz = classes[i];
                HomoSerializationProcessor serializationProcessor;
                if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)) {
                    serializationProcessor = new ProtoSerializationProcessor();
                    paramContentType = RpcContentType.BYTES;
                    paramContent = new ByteRpcContent();
                } else if (JSONObject.class.isAssignableFrom(clazz) || JSONArray.class.isAssignableFrom(clazz)) {
                    serializationProcessor = new FastjsonSerializationProcessor();
                    paramContentType = RpcContentType.JSON;
                    paramContent = new JsonRpcContent();
                } else if (UploadFile.class.isAssignableFrom(clazz)) {
                    serializationProcessor = null;
                    paramContentType = RpcContentType.FILE;
                    paramContent = new FileRpcContent();
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
            log.error("exportParamsSerializeInfo error", e);
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

    public Object[] covertToActualParam(RpcContent rpcContent) {
        return covertToActualParam(null, null,rpcContent);
    }
    public Object[] covertToActualParam(Integer podId, ParameterMsg parameterMsg, RpcContent rpcContent) {
        int paramCount = paramSerializeInfos.length;
        if (paramCount <= 0) {
            return null;
        }
        return rpcContent.unSerializeToActualParams(paramSerializeInfos, paddingOffset, podId, parameterMsg);
    }

    public RpcContent warpToRpcContent(String funName, Object[] rawParams) {
        RpcContent rpcContent;
        if (paramContentType.equals(RpcContentType.BYTES)) {
            rpcContent = new ByteRpcContent();
        } else if (paramContentType.equals(RpcContentType.JSON)) {
            rpcContent = new JsonRpcContent();
        } else if (paramContentType.equals(RpcContentType.FILE)) {
            rpcContent = new FileRpcContent();
        }else {
            rpcContent = new ByteRpcContent();
        }
        rpcContent.setMsgId(funName);
        rpcContent.setReturnType(returnSerializeInfo.getParamType());
        rpcContent.setParam(rpcContent.serializeRawParams(rawParams, paramSerializeInfos, paddingOffset));
        return rpcContent;
    }


    public Object serializeForReturn(Object returnValue) {
        Object object = returnContent.serializeReturn(returnValue, returnSerializeInfo);
        return object;
    }

}
