package com.homo.demo.rpc.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.utils.rector.Homo;
import com.homo.demo.http.rpc.server.facade.HttpRpcDemoServiceFacade;
import io.homo.demo.proto.HttpServerRequestPb;
import io.homo.demo.proto.HttpServerResponsePb;
import io.homo.proto.rpc.HttpHeadInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 继承BaseService提供基础服务
 * 并实现IHttpRpcDemoService接口
 */
@Slf4j
@Component
public class RpcHttpService extends BaseService implements HttpRpcDemoServiceFacade {
    @Override
    public Homo<JSONObject> jsonGetJson(JSONObject header) {
        log.info("jsonGetJson header {}",header);
        JSONObject res= new JSONObject();
        res.put("result","success");
        return Homo.result(res);
    }


    @Override
    public Homo<JSONObject> jsonPost(JSONObject header,JSONObject req) {
        log.info("jsonPost req {} header {}",req,header);
        JSONObject res= new JSONObject();
        res.put("result","success");
        return Homo.result(res);
    }

    @Override
    public Homo<JSONObject> jsonPostArray(JSONObject header, JSONArray jsonArray) {
        log.info("jsonArray {} header {}",jsonArray,header);
        JSONObject res= new JSONObject();
        res.put("result","success");
        return Homo.result(res);
    }

    @Override
    public Homo<String> valuePost(JSONObject header, String value) {
        log.info("valuePost value {} header {}",value,header);
        JSONObject res= new JSONObject();
        res.put("result","success");
        return Homo.result(res.toJSONString());
    }

    @Override
    public Homo<HttpServerResponsePb> pbPost(HttpHeadInfo headerInfo, HttpServerRequestPb req) {
        log.info("pbPost req {} headerInfo {}",req,headerInfo);
        HttpServerResponsePb response = HttpServerResponsePb.newBuilder().setCode(1).build();
        return Homo.result(response);
    }

}
