package com.homo.core.rpc.base.serial;

import com.homo.core.facade.security.RpcSecurity;
import com.homo.core.rpc.base.security.AccessControl;
import com.homo.core.utils.reflect.HomoTypeUtil;
import com.homo.core.utils.serial.FSTSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.ProtoSerializationProcessor;
import io.homo.proto.client.ParameterMsg;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiPredicate;

@Slf4j
public class MethodDispatchInfo implements RpcSecurity {
    private Method method;
    private int paramCount;
    private SerializeInfo[] paramSerializeInfos;
    private SerializeInfo[] returnSerializeInfos;
    private AccessControl accessControl;
    // 向参数数组中填充podId和parameterMsg的标志
    private boolean paddingParams = false;
    private final BiPredicate<Object,Object> needFillParamsPredicate = (o1, o2) -> {
        // 如果方法的参数列表第一个是Integer类型，第二个是是ParameterMsg，则设置填充参数标志为true
        return o1.getClass().isAssignableFrom(Integer.class) && o2.getClass().isAssignableFrom(ParameterMsg.class);
    };

    private MethodDispatchInfo(Method method) {
        this.method = method;
        this.init();
    }

    private void init() {
        this.paramCount = exportParamCount();
        this.paramSerializeInfos = exportParamSerializeInfos();
        this.returnSerializeInfos = exportReturnSerializeInfos();
    }

    private SerializeInfo[] exportParamSerializeInfos() {
        Class<?>[] classes = method.getParameterTypes();
        if (classes == null) {
            return null;
        }
        try {
            SerializeInfo[] serializeInfos = new SerializeInfo[classes.length];
            for (int i = 0; i < classes.length; i++) {
                Class<?> clazz = classes[i];
                serializeInfos[i] = exportSerializeInfo(clazz,null);
            }
            if (needFillParamsPredicate.test(serializeInfos[0].paramType,serializeInfos[1].paramType)){
                paddingParams = true;
            }
            return serializeInfos;
        } catch (Exception e) {
            log.error("exportSerializeInfos error", e);
            return null;
        }
    }

    private SerializeInfo[] exportReturnSerializeInfos() {
        Class<?>[] classes = getTypeParamsByType(method.getReturnType());
        if (classes == null) {
            return null;
        }
        try {

            SerializeInfo[] serializeInfos = new SerializeInfo[classes.length];
            for (int i = 0; i < classes.length; i++) {
                Class<?> clazz = classes[i];
                if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(clazz)){
                    ProtoSerializationProcessor protoSerializationProcessor = new ProtoSerializationProcessor();
                    serializeInfos[i] = exportSerializeInfo(clazz,protoSerializationProcessor);
                }else {
                    FSTSerializationProcessor fstSerializationProcessor = new FSTSerializationProcessor();
                    serializeInfos[i] = exportSerializeInfo(clazz,fstSerializationProcessor);
                }

            }
            return serializeInfos;
        } catch (Exception e) {
            log.error("exportSerializeInfos error", e);
            return null;
        }
    }



    private SerializeInfo exportSerializeInfo(Class<?> clazz, HomoSerializationProcessor serializationProcessor) {
        SerializeInfo serializeInfo = SerializeInfo.create(clazz,serializationProcessor);
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
        MethodDispatchInfo methodDispatchInfo = new MethodDispatchInfo(method);
        methodDispatchInfo.accessControl = AccessControl.create(method);
        return methodDispatchInfo;
    }

    @Override
    public boolean isCallAllowed(String srcServiceName) {
        return accessControl.isCallAllowed(srcServiceName);
    }
}