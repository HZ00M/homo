package com.homo.entity.client;

import com.homo.core.entity.client.IEntityClientService;
import com.homo.core.entity.client.TestEntityClientApplication;
import io.homo.proto.client.ParameterMsg;
import io.homo.proto.entity.test.AccountInfo;
import io.homo.proto.entity.test.TestEntityRequest;
import io.homo.proto.entity.test.TestEntityResponse;
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
@SpringBootTest(classes = TestEntityClientApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityClientTest {
    @Autowired(required = false)
    IEntityClientService entityClientService;
    String userId = "124";
    String userId2 = "456";
    @Test
    public void localEntityCall() throws InterruptedException {
        StepVerifier.create(entityClientService.login(0, ParameterMsg.newBuilder().setUserId(userId).build(), UserLoginRequest.newBuilder().build())
                        .nextValue(ret -> {
                            log.info("jsonGet ret {}", ret);
                            return ret.getCode();
                        }))
                .expectNext(0)
                .verifyComplete();
        StepVerifier.create(entityClientService.remoteEntityCall(ParameterMsg.newBuilder().setUserId(userId).build())
                        .consumerValue(ret -> {
                            log.info("jsonGet ret {}", ret);
                        }))
                .expectNext("success")
                .verifyComplete();
//        Thread.currentThread().join(1000000);
    }

    @Test
    public void remoteEntityCall() throws InterruptedException {
        StepVerifier.create(entityClientService.login(0, ParameterMsg.newBuilder().setUserId(userId2).build(), UserLoginRequest.newBuilder().build())
                        .nextValue(ret -> {
                            log.info("jsonGet ret {}", ret);
                            return ret.getCode();
                        }))
                .expectNext(0)
                .verifyComplete();
//        Thread.currentThread().join(1000000);
    }

    @Test
    public void remoteEntityCall2() throws InterruptedException {
        StepVerifier.create(entityClientService.login(0, ParameterMsg.newBuilder().setUserId(userId).build(), UserLoginRequest.newBuilder().setLoginAccount(AccountInfo.newBuilder().setUserId(userId).build()).build())
                        .nextValue(ret -> {
                            log.info("jsonGet ret {}", ret);
                            return ret.getCode();
                        }))
                .expectNext(0)
                .verifyComplete();
        StepVerifier.create(entityClientService.remoteEntityCall(0,ParameterMsg.newBuilder().setUserId(userId).build(), TestEntityRequest.newBuilder().setParam("123").build())
                        .consumerValue(ret -> {
                            log.info("jsonGet ret {}", ret);
                        }))
                .expectNext(TestEntityResponse.newBuilder().setCode(1).build())
                .verifyComplete();
//        Thread.currentThread().join(1000000);
    }
}
