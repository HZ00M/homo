package com.homo.core.entity.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.concurrent.CountDownLatch;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EntityServerDemoApplication {
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    public static void main(String[] args) {
        SpringApplication.run(EntityServerDemoApplication.class);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("main error :", e);
        }
    }
}
