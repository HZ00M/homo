package com.homo.core.rpc.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.client.TestRpcClientApplication;
import com.homo.core.rpc.server.facade.RpcServiceFacade;
import com.homo.core.rpc.server.facade.TestObjParam;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
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
    @Autowired(required = false)
    RpcServiceFacade rpcService;

    @Test
    public void testPbCall1()  {
        StepVerifier.create(
                rpcService.pbCall(0, ParameterMsg.newBuilder().setUserId("1_1").build(),TestServerRequest.newBuilder().setParam("123").build())
                        .nextValue(TestServerResponse::getCode)
        )
                .expectNext(123)
                .verifyComplete();
    }

    @Test
    public void testPbCall2()  {
        StepVerifier.create(
                rpcService.pbCall(-1, ParameterMsg.newBuilder().setUserId("1_1").build(),TestServerRequest.newBuilder().setParam("123").build())
                        .nextValue(TestServerResponse::getCode)
        )
                .expectNext(123)
                .verifyComplete();
    }

    @Test
    public void testObjCall()  {
        TestObjParam testObjParam = new TestObjParam();
        StepVerifier.create(rpcService.objCall(0, ParameterMsg.newBuilder().build(),testObjParam)
        )
                .expectNext(1)
                .verifyComplete();

    }

    @Test
    public void testJsonCall()  {
        TestObjParam testObjParam = new TestObjParam();
        String jsonString = JSONObject.toJSONString(testObjParam);
        StepVerifier.create(rpcService.jsonCall(0, ParameterMsg.newBuilder().build(),jsonString)
                .consumerValue(ret->{
                    log.info("jsonCall ret {}",ret);
                })
        )
                .expectNext()
                .verifyComplete();
    }
}
