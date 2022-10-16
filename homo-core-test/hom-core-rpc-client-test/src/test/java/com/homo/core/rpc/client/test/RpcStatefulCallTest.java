package com.homo.core.rpc.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.client.TestRpcClientApplication;
import com.homo.core.rpc.server.facade.RpcStatefulServiceFacade;
import com.homo.core.rpc.server.facade.TestObjParam;
import io.homo.proto.client.ParameterMsg;
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
public class RpcStatefulCallTest {
    @Autowired(required = false)
    RpcStatefulServiceFacade rpcService;

    @Test
    public void testPbCall1() throws InterruptedException {
        rpcService.pbCall(0, ParameterMsg.newBuilder().setUserId("1_1").build(),TestServerRequest.newBuilder().setParam("123").build())
                .consumerValue(ret->{
                    log.info("pbCall ret {}",ret);
                })
                .start();
        Thread.currentThread().join();
    }

    @Test
    public void testPbCall2() throws InterruptedException {
        rpcService.pbCall(-1, ParameterMsg.newBuilder().setUserId("1_1").build(),TestServerRequest.newBuilder().setParam("123").build())
                .consumerValue(ret->{
                    log.info("pbCall ret {}",ret);
                })
                .start();
        Thread.currentThread().join();
    }

    @Test
    public void testObjCall() throws InterruptedException {
        TestObjParam testObjParam = new TestObjParam();
        rpcService.objCall(0, ParameterMsg.newBuilder().build(),testObjParam)
                .consumerValue(ret->{
                    log.info("objCall ret {}",ret);
                }).start();

        Thread.currentThread().join();
    }

    @Test
    public void testJsonCall() throws InterruptedException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("1","1");
        rpcService.jsonCall(0, ParameterMsg.newBuilder().build(),jsonObject)
                .consumerValue(ret->{
                    log.info("jsonCall ret {}",ret);
                }).start();
        Thread.currentThread().join();
    }
}
