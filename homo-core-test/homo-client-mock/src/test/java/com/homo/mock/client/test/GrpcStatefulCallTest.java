package com.homo.mock.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.server.facade.GrpcStatefulServiceFacade;
import com.homo.core.rpc.server.vo.ParamVO;
import com.homo.mock.client.ClientMockApplication;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
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
public class GrpcStatefulCallTest {
    /**
     * 使用目标服务器接口发起远程调用
     */
    @Autowired(required = false)
    GrpcStatefulServiceFacade rpcService;

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
        ParamVO paramVO = new ParamVO();
        StepVerifier.create(
                rpcService.objCall(0, ParameterMsg.newBuilder().build(), paramVO)
        )
                .expectNext(1)
                .verifyComplete();

    }

    @Test
    public void testJsonCall()  {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("1","1");
        StepVerifier.create(
                rpcService.jsonCall(0, ParameterMsg.newBuilder().build(),jsonObject)
                .nextValue(ret-> ret.get("2"))
        )
                .expectNext("2")
                .verifyComplete();
    }
}
