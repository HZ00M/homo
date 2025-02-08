package com.homo.mock.client.test;

import com.homo.mock.client.ClientMockApplication;
import com.homo.mock.client.facade.ClientEntityServiceFacade;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.EntityRequest;
import io.homo.proto.entity.test.UserLoginRequest;
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
public class EntityTest {
    @Autowired(required = false)
    ClientEntityServiceFacade entityClientService;
    String userId = "124";
    String userId2 = "457";

    @Test
    public void loginAndQuery() throws InterruptedException {
        StepVerifier.create(entityClientService.login(0, ParameterMsg.newBuilder().setUserId(userId).build(), UserLoginRequest.newBuilder().build())
                        .nextValue(ret -> {
                            log.info("login ret {}", ret);
                            return ret.getCode();
                        }))
                .expectNext(0)
                .verifyComplete();
        StepVerifier.create(entityClientService.queryRemoteServerInfo(
                                        0,
                                        ParameterMsg.newBuilder().setUserId(userId).build()
                                )
                                .consumerValue(ret -> {
                                    log.info("queryRemoteServerInfo ret {}", ret);
                                })
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
        StepVerifier.create(entityClientService.queryRemoteServerInfo(
                                        0,
                                        ParameterMsg.newBuilder().setUserId(userId).build()
                                )
                                .consumerValue(ret -> {
                                    log.info("queryRemoteServerInfo ret {}", ret);
                                })
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }

    @Test
    public void innerRpcCall() throws InterruptedException {
        StepVerifier.create(entityClientService.innerRpcCall(0, ParameterMsg.newBuilder().setUserId(userId2).build())
                        .consumerValue(ret -> {
                            log.info("innerRpcCall ret {}", ret);
                        }))
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }

    @Test
    public void innerCallAndRemoteCall() throws InterruptedException {
        StepVerifier.create(
                        entityClientService.innerCallAndRemoteCall(ParameterMsg.newBuilder().setUserId(userId).build())
                                .consumerValue(ret -> {
                                    log.info("innerCallAndRemoteCall ret {}", ret);
                                })
                )
                .expectNextMatches(ret -> true)
                .verifyComplete();
    }

}
