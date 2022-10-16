package com.homo.core.rpc.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.client.TestRpcClientApplication;
import com.homo.core.rpc.server.facade.RpcServiceFacade;
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
public class RpcStatelessCallTest {
    @Autowired
    RpcServiceFacade rpcService;

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
        StepVerifier.create(rpcService.objCall(0, ParameterMsg.newBuilder().build(),testObjParam)
                .consumerValue(ret->{
                    log.info("objCall ret {}",ret);
                })
        )

                .expectNext()
                .verifyComplete();
        Thread.currentThread().join();
    }

    @Test
    public void testJsonCall() throws InterruptedException {
        TestObjParam testObjParam = new TestObjParam();
        String jsonString = JSONObject.toJSONString(testObjParam);
        StepVerifier.create(rpcService.jsonCall(0, ParameterMsg.newBuilder().build(),jsonString)
                .consumerValue(ret->{
                    log.info("jsonCall ret {}",ret);
                })
        )
                .expectNext()
                .verifyComplete();
        Thread.currentThread().join();
    }
}
