package com.homo.core.persistent.test;

import com.homo.service.dirty.anotation.DirtyLandingServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
@DirtyLandingServer
public class PersistentTessApplication implements CommandLineRunner {

        public static void main(String[] args) {
            SpringApplication.run(PersistentTessApplication.class);
        }

        @Override
        public void run(String... args) {}
}
