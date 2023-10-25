package com.homo.core.rpc.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.base.service.BaseService;
import com.homo.core.rpc.server.facade.RpcHttpServiceFacade;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.rpc.HttpHeadInfo;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RpcHttpService extends BaseService implements RpcHttpServiceFacade {
    @Override
    public Homo<JSONObject> jsonGetJson(JSONObject header) {
        log.info("jsonGet header {}",header);
        JSONObject res= new JSONObject();
        res.put("key","value");
        return Homo.result(res);
    }

    @Override
    public Homo<String> jsonGetStr(JSONObject header) {
        log.info("jsonGet header {}",header);
        String s = "测试";
        return Homo.result(s);
    }

    @Override
    public Homo<String> jsonPost(JSONObject header,JSONObject req) {
        log.info("jsonPost req {} header {}",req,header);
        JSONObject res= new JSONObject();
        res.put("key","value");
        return Homo.result(res.toJSONString());
    }

    @Override
    public Homo<String> jsonPostArray(JSONObject header, JSONArray jsonArray) {
        log.info("jsonArray {} header {}",jsonArray,header);
        JSONObject res= new JSONObject();
        res.put("key","value");
        return Homo.result(res.toJSONString());
    }

    @Override
    public Homo<String> postValue(JSONObject header,String value) {
        log.info("postValue value {} header {}",value,header);
        JSONObject res= new JSONObject();
        res.put("key","value");
        return Homo.result(res.toJSONString());
    }

    @Override
    public Homo<TestServerResponse> pbPost(HttpHeadInfo header,TestServerRequest req) {
        log.info("pbPost req {} header {}",req,header);
        TestServerResponse response = TestServerResponse.newBuilder().setCode(1).build();
        return Homo.result(response);
    }
}
