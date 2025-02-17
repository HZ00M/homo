package com.homo.mock.client.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.rpc.server.facade.GrpcRpcServiceFacade;
import com.homo.core.rpc.server.vo.ParamVO;
import com.homo.document.mongo.facade.MongoDocumentServiceFacade;
import com.homo.mock.client.ClientMockApplication;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.document.demo.CreateUserReq;
import io.homo.proto.document.demo.CreateUserResp;
import io.homo.proto.document.demo.GetUserInfoReq;
import io.homo.proto.document.demo.GetUserInfoResp;
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
public class MongoDocumentTest {
    @Autowired(required = false)
    MongoDocumentServiceFacade serviceFacade;

    @Test
    public void createUserInfo() {
        CreateUserReq req = CreateUserReq.newBuilder().setUserId("user_123").build();
        StepVerifier.create(
                        serviceFacade.createInfo(req)
                                .consumerValue(ret -> {
                                    log.info("createInfo ret {}", ret);
                                })
                                .nextValue(CreateUserResp::getErrorCode)
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }
    @Test
    public void queryUserInfo() {
        GetUserInfoReq req = GetUserInfoReq.newBuilder().setUserId("user_123").build();
        StepVerifier.create(
                        serviceFacade.getUserInfo(req)
                                .consumerValue(ret -> {
                                    log.info("getUserInfo ret {}", ret);
                                })
                                .nextValue(GetUserInfoResp::getErrorCode)
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }
}
