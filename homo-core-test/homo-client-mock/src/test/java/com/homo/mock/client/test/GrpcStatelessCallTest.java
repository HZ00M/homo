package com.homo.mock.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.server.facade.GrpcRpcServiceFacade;
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
//@RunWith(SpringJUnit4ClassRunner.class)
public class GrpcStatelessCallTest {
    @Autowired(required = false)
    GrpcRpcServiceFacade rpcService;

    @Test
    public void pbCall() {
        StepVerifier.create(
                        rpcService.pbCall(0,
                                        ParameterMsg.newBuilder().setUserId("1_1").build(),
                                        TestServerRequest.newBuilder().setParam("123").build())
                                .consumerValue(ret -> {
                                    log.info("pbCall ret {}", ret);
                                })
                                .nextValue(TestServerResponse::getCode)
                )
                .expectNextMatches(ret -> true)
//                .expectNext(123)
                .verifyComplete();
    }

    @Test
    public void tuple2ReturnCall() {
        StepVerifier.create(
                        rpcService.tuple2ReturnCall(-1, ParameterMsg.newBuilder().setUserId("1_1").build())
                                .consumerValue(ret->{
                                    log.info("tuple2ReturnCall ret {}", ret);
                                })
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }

    @Test
    public void objCall() {
        ParamVO paramVO = new ParamVO();
        StepVerifier.create(rpcService.objCall(0, ParameterMsg.newBuilder().setUserId("123").build(), paramVO)
                        .consumerValue(ret->{
                            log.info("objCall ret {}", ret);
                        })
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();

    }

    @Test
    public void valueCall() {
        ParamVO paramVO = new ParamVO();
        String jsonString = JSONObject.toJSONString(paramVO);
        StepVerifier.create(rpcService.valueCall(0, ParameterMsg.newBuilder().setChannelId("111").build(), jsonString)
                        .consumerValue(ret -> {
                            log.info("jsonCall ret {}", ret);
                        })
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }
}
