package com.homo.core.rpc.client;

import com.homo.core.rpc.server.facade.GrpcStatefulServiceFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestRpcClientApplication implements CommandLineRunner {
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    @Autowired
    GrpcStatefulServiceFacade rpcService;
    public static void main(String[] args) {
        SpringApplication.run(TestRpcClientApplication.class);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("main error :", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
//        rpcService.pbCall(TestServerRequest.newBuilder().build()).start();
    }
}
