package com.homo.core.rpc.http.mapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.configurable.rpc.RpcHttpServerProperties;
import com.homo.core.utils.module.DriverModule;
import com.homo.core.rpc.http.HttpServer;
import com.homo.core.rpc.http.RpcServerFactoryHttpImpl;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

@Slf4j
public class AbstractHttpMapping implements DriverModule {

    protected volatile Map<Integer, HttpServer> routerHttpServerMap;
    protected static final String HOMO_REMOTE_ADDRESS = "HOMO_REMOTE_ADDRESS";
    protected static RpcHttpServerProperties rpcHttpServerProperties;
    public void moduleInit(){
        routerHttpServerMap = RpcServerFactoryHttpImpl.httpServerMap;
        rpcHttpServerProperties = GetBeanUtil.getBean(RpcHttpServerProperties.class);
    }


    protected static void checkDataBufferSize(DataBuffer dataBuffer)throws Exception{
        int byteCount = dataBuffer.readableByteCount();
        if(byteCount > rpcHttpServerProperties.getBytesLimit()){
            String errorInfo = "bytesLength > bytesLimit length("+ byteCount + ") > limit(" + rpcHttpServerProperties.getBytesLimit() + ")";
            log.error(errorInfo);
            throw HomoError.throwError(HomoError.httpMaxByteError,byteCount,rpcHttpServerProperties.getBytesLimit());
        }
    }
    /**
     * 构造参数json数组
     *
     */
     protected String buildJsonListMsg(Object... objs) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, objs);
        return JSON.toJSONString(list);
    }

    public static int exportPort(ServerHttpRequest request){
        int port = Objects.requireNonNull(request.getLocalAddress()).getPort();
        return port;
    }



    protected Map<String, String> exportQueryParams(ServerHttpRequest request) {
        return request.getQueryParams().toSingleValueMap();
    }

    /**
     * 消息头转json
     *
     * @param request
     * @return
     */
    protected JSONObject exportHeaderInfo(ServerHttpRequest request){
        String hostAddress = getHostAddress(request);
        Map<String, String> headerInfo = request.getHeaders().toSingleValueMap();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(headerInfo);
        jsonObject.put(HOMO_REMOTE_ADDRESS,hostAddress);
        return jsonObject;
    }

    /**
     * 获取地址信息
     * @param request
     * @return
     */
    protected String getHostAddress(ServerHttpRequest request){
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress == null){
            return null;
        }
        InetAddress address = remoteAddress.getAddress();
        if (address==null){
            return null;
        }
        return address.getHostAddress();
    }
}
