package com.homo.core.rpc.client.test;

import com.homo.core.rpc.client.TestRpcClientApplication;
import com.homo.core.rpc.client.facade.RpcClientFacade;
import com.homo.core.rpc.server.facade.RpcServiceFacade;
import io.homo.proto.rpc.test.TestServerRequest;
import io.homo.proto.rpc.test.TestServerResponse;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

@Log4j2
@SpringBootTest(classes = TestRpcClientApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@RunWith(SpringJUnit4ClassRunner.class)
public class RpcCallTest {
    @Autowired
    RpcServiceFacade rpcService;

    @Test
    public void testPbCall(){
        StepVerifier.create(rpcService.pbCall(TestServerRequest.newBuilder().build()))
                .expectNext(TestServerResponse.newBuilder().build())
                .verifyComplete();
    }

    @Test
    public void testJsonCall(){
        StepVerifier.create(rpcService.jsonCall(""))
                .expectNext()
                .verifyComplete();
    }
}
