package com.homo.core.rpc.client;

import com.homo.core.rpc.server.facade.RpcStatefulServiceFacade;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.concurrent.CountDownLatch;

@Log4j2
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestRpcClientApplication implements CommandLineRunner {
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    @Autowired
    RpcStatefulServiceFacade rpcService;
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
