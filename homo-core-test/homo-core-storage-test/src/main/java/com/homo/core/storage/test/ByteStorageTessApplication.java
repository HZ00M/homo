package com.homo.core.storage.test;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

;

@Log4j2
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
