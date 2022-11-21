package com.homo.core.rpc.server.facade;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.rpc.HttpHeadInfo;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;

/**
 * 声明一个http无状态服务
 */
@ServiceExport(tagName = "http-server:30013",isMainServer = false,isStateful = false,driverType = RpcType.http)
@RpcHandler
public interface RpcHttpServiceFacade {

    Homo<JSONObject> jsonGetJson(JSONObject header);

    Homo<String> jsonGetStr(JSONObject header);

    Homo<String> jsonPost(JSONObject header, JSONObject req);

    Homo<String> jsonPostArray(JSONObject header, JSONArray jsonArray);

    Homo<String> postValue(JSONObject header,String value);

    Homo<TestServerResponse> pbPost(HttpHeadInfo header,TestServerRequest req);

}
