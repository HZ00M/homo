package com.homo.demo.http.rpc.server.facade;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.demo.proto.HttpServerRequestPb;
import io.homo.demo.proto.HttpServerResponsePb;
import io.homo.proto.rpc.HttpHeadInfo;

/**
 * 声明一个http无状态的主服务
 * host http-rpc-demo-server
 * 端口 30300
 */
@ServiceExport(tagName = "http-rpc-demo-server:30300",isMainServer = true,isStateful = false,driverType = RpcType.http)
@RpcHandler
public interface HttpRpcDemoServiceFacade {

    /**
     * get 请求
     * @param header 请求头
     * @return 返回 JSONObject
     */
    Homo<JSONObject> jsonGetJson(JSONObject header);

    /**
     * post 请求
     * @param header 请求头
     * @param req 请求体
     * @return 返回 String
     */
    Homo<JSONObject> jsonPost(JSONObject header, JSONObject req);

    /**
     * post 请求
     * @param header 请求头
     * @param jsonArray 请求体
     * @return 返回 String
     */
    Homo<JSONObject> jsonPostArray(JSONObject header, JSONArray jsonArray);

    /**
     * post 请求
     * @param header 请求头
     * @param value 请求值
     * @return 返回 String
     */
    Homo<String> valuePost(JSONObject header, String value);

    /**
     * post 请求 (pb协议)
     * @param headerInfo 通用的pb请求头
     * @param req 自定义pb请求体
     * @return
     */
    Homo<HttpServerResponsePb> pbPost(HttpHeadInfo headerInfo, HttpServerRequestPb req);

}
