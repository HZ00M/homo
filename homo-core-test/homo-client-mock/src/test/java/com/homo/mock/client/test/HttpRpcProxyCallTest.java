package com.homo.mock.client.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homo.demo.http.rpc.server.facade.HttpRpcDemoServiceFacade;
import com.homo.demo.http.rpc.server.vo.TestObjParam;
import com.homo.mock.client.ClientMockApplication;
import io.homo.demo.proto.HttpServerRequestPb;
import io.homo.proto.rpc.HttpHeadInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@Slf4j
@SpringBootTest(classes = ClientMockApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpRpcProxyCallTest {
    @Autowired(required = false)
    HttpRpcDemoServiceFacade rpcDemoService;

    /**
     * 返回值为JSON值类型  post调用与get调用都能支持
     * 因此可以使用rpc代理进行调用
     * 使用rpc代理默认是通过post调用
     * @throws InterruptedException
     */
    @Test
    public void JsonGet() throws InterruptedException {

        JSONObject header = new JSONObject();
        header.put("token", "123");
        StepVerifier.create(rpcDemoService.jsonGetJson(header)
                        .consumerValue(ret -> {
                            log.info("jsonGetJson ret {}", ret);
                        }))
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }


    @Test
    public void JsonPost() throws InterruptedException {
        TestObjParam testObjParam = new TestObjParam();
        JSONObject header = new JSONObject();
        header.put("token", "123");
        StepVerifier.create(rpcDemoService.jsonPost(JSON.parseObject(JSONObject.toJSONString(testObjParam)), header)
                        .consumerValue(ret -> {
                            log.info("jsonPost ret {}", ret);
                        }))
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }

    @Test
    public void PbPost() throws InterruptedException {
        HttpHeadInfo header = HttpHeadInfo.newBuilder().putHeaders("key", "value").build();
        HttpServerRequestPb request = HttpServerRequestPb.newBuilder().setParam("123").build();
        StepVerifier.create(rpcDemoService.pbPost(header, request)
                        .consumerValue(ret -> {
                            log.info("objCall ret {}", ret);
                        }))
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }
}
