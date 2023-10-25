package com.homo.core.storage.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
//@DirtyLandingServer
public class ByteStorageTessApplication implements CommandLineRunner {

        public static void main(String[] args) {
            SpringApplication.run(ByteStorageTessApplication.class);
        }

        @Override
        public void run(String... args) {

        }

}
