package com.homo.core.gate.tcp;

import com.homo.core.facade.gate.GateDriver;
import com.homo.core.gate.GateServerMgr;
import com.homo.core.gate.tcp.handler.TestFastJsonGateLogicHandler;
import com.homo.core.gate.tcp.handler.TestProtoGateLogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TcpGateServerApplication implements CommandLineRunner {
    static CountDownLatch countDownLatch = new CountDownLatch(1);

    @Autowired(required = false)
    private GateDriver tcpGateDriver;
    @Autowired(required = false)
    private GateServerMgr gateServerMgr;
    @Autowired
    TestProtoGateLogicHandler testProtoLogicHandler;
    @Autowired
    TestFastJsonGateLogicHandler testJsonLogicHandler;

    public static void main(String[] args) {
        SpringApplication.run(TcpGateServerApplication.class);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("main error :", e);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        ((TcpGateDriver)tcpGateDriver).registerPostHandler(testProtoLogicHandler);
        ((TcpGateDriver)tcpGateDriver).registerPostHandler(testJsonLogicHandler);
        gateServerMgr.startGateServer("tcpProxy", 30033);
    }
}
