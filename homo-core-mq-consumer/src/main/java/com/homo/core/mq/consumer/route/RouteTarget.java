package com.homo.core.mq.consumer.route;

import com.homo.core.facade.mq.consumer.ConsumerCallback;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import com.homo.core.facade.mq.consumer.SinkHandler;
import com.homo.core.utils.reflect.HomoTypeUtil;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 路由信息类。每一个实例对应一个处理函数
 */
@ToString
@Slf4j
public class RouteTarget {
    private ReceiverSink receiverSink;
    private SinkHandler handler;
    private Method func;
    @Getter
    private Class<?> messageClazz;
    private final TargetType targetType;

    public RouteTarget(ReceiverSink receiverSink) throws Exception {
        this.receiverSink = receiverSink;

        SerializedLambda serializedLambda =  HomoTypeUtil.getSerializedLambda(receiverSink);
        if (serializedLambda != null){
            List<Class<?>> lambdaParameterTypes = HomoTypeUtil.getLambdaParameterTypes(serializedLambda);
            this.messageClazz = lambdaParameterTypes.get(1);
        }else {
            this.messageClazz = HomoTypeUtil.getParamClasses(receiverSink.getClass().getMethod("onSink", String.class, Serializable.class, ConsumerCallback.class))[1];
        }
        this.targetType = TargetType.SINK;
    }

    public RouteTarget(SinkHandler handler, Method func) {
        this.handler = handler;
        this.func = func;
        this.messageClazz = func.getParameterTypes()[1];
        this.targetType = TargetType.HANDLER;
    }

    public void invoke(String realTopic, Serializable message, ConsumerCallback callback) throws Exception{
        if (targetType.equals(TargetType.SINK)) {
            receiverSink.onSink(realTopic, message, callback);
        } else {
            func.invoke(handler, realTopic, message, callback);
        }
    }

    public enum TargetType {
        SINK,
        HANDLER
    }

    public static @Nullable <T extends Serializable> SerializedLambda getSerializedLambda(ReceiverSink<T> functionInterface) {
        try {
            //MessageSink继承了Serializable接口, 如果是Lambda一定会实现一个无参方法writeReplace。
            Method method = functionInterface.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return  (SerializedLambda) method.invoke(functionInterface);
        } catch (Exception e) {
            //调用出错，那么确定MessageSink不是Lambda表达式实现
            return null;
        }
    }
    /**
     * 获取MessageSink Lambda表达式的参数类型
     * 要获取Lambda表达式对应的参数类型和返回值类型，可使用其中的instantiatedMethodType属性，它的一般形式如下：
     * (Ljava/lang/Integer;Ljava/lang/Double;)Ljava/lang/String;
     * 共中，括号里面的是参数类型，括号外面的是返回值类型，每个类型都以L开头，以分号结尾。以上字符串表明当前Lambda函数的参数类型是[java.lang.Integer, java.lang.Double]，返回值类型是java.lang.String。很容易使用正则表达式解析出对应的信息。
     */
    public static List<Class<?>> getLambdaParameterTypes(SerializedLambda serializedLambda) {
        String expr = serializedLambda.getInstantiatedMethodType();
        Matcher matcher = PARAMETER_TYPE_PATTERN.matcher(expr);
        if (!matcher.find() || matcher.groupCount() != 1) {
            throw new RuntimeException("获取Lambda信息失败");
        }
        //此时取到刮号中的参数字符串  Ljava/lang/Integer;Ljava/lang/Double;
        expr = matcher.group(1);

        //按分号分隔字符串
        return Arrays.stream(expr.split(";"))
                .filter(s -> !s.isEmpty())
                .map(s ->{
                            //去掉每一个参数全类名的为L的第一个字母
                            if(s.startsWith("L")){
                                s=s.substring(1);
                            }
                            return s.replace("/", ".");
                        }
                )
                .map(s -> {
                    try {
                        return Class.forName(s);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("无法加载类:"+s, e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static Pattern PARAMETER_TYPE_PATTERN = Pattern.compile("\\((.*)\\).*");
}
