package com.homo.core.rpc.http.mapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.utils.module.DriverModule;
import com.homo.core.configurable.rpc.RpcHttpServerProperties;
import com.homo.core.rpc.http.HttpServer;
import com.homo.core.rpc.http.RpcServerFactoryHttpImpl;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;

@Log4j2
public class AbstractHttpMapping implements DriverModule {

    protected volatile Map<Integer, HttpServer> routerHttpServerMap;
    protected static final String HOMO_REMOTE_ADDRESS = "HOMO_REMOTE_ADDRESS";
    protected static RpcHttpServerProperties rpcHttpServerProperties;
    public void init(){
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

    /**
     * 分割‘/’，‘-’字符，首字符大写，拼接成msgId
     * @param request
     * @return
     */
    public static String exportMsgId(ServerHttpRequest request){
        String url = request.getURI().getPath();
        url = url.substring(1);
        url = url.replace("-", "/");
        String[] split = url.split("/");
        StringBuilder msgIdBuilder = new StringBuilder(split[0]);
        for(int i = 1; i < split.length; i++){
//            String tmp = StringUtils.toUpperCase4Index(split[i]);
//            msgIdBuilder.append(tmp);
            msgIdBuilder.append(split[i]);
        }
        return msgIdBuilder.toString();
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
