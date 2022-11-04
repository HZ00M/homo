package com.homo.core.rpc.client.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.client.TestRpcClientApplication;
import com.homo.core.rpc.server.facade.RpcHttpServiceFacade;
import com.homo.core.rpc.server.facade.TestObjParam;
import io.homo.proto.rpc.HttpHeadInfo;
import io.homo.proto.rpc.test.TestServerRequest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@Log4j2
@SpringBootTest(classes = TestRpcClientApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@RunWith(SpringJUnit4ClassRunner.class)
public class RpcHttpCallTest {
    @Autowired(required = false)
    RpcHttpServiceFacade rpcService;

    @Test
    public void testJsonGet() throws InterruptedException {

        JSONObject header = new JSONObject();
        header.put("token","123");
        StepVerifier.create(rpcService.jsonGetJson(header)
                .consumerValue(ret->{
                    log.info("jsonGet ret {}",ret);
                }))
        .verifyComplete();
    }

    @Test
    public void testJsonPost() throws InterruptedException {
        TestObjParam testObjParam = new TestObjParam();
        JSONObject header = new JSONObject();
        header.put("token","123");
        StepVerifier.create(rpcService.jsonPost(JSON.parseObject(JSONObject.toJSONString(testObjParam)),header)
                .consumerValue(ret->{
                    log.info("jsonPost ret {}",ret);
                }))
                .verifyComplete();
    }

    @Test
    public void testPbPost() throws InterruptedException {
        HttpHeadInfo header = HttpHeadInfo.newBuilder().putHeaders("key","value").build();
        TestServerRequest request = TestServerRequest.newBuilder().setParam("123").build();
        StepVerifier.create(rpcService.pbPost(header,request)
                .consumerValue(ret->{
                    log.info("objCall ret {}",ret);
                }))
                .verifyComplete();
    }


}
