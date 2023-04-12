package test;

import com.homo.core.tread.tread.intTread.IntTreadMgr;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication(scanBasePackages = "com.syyx.tpf")
public class TreadTestApplication implements CommandLineRunner {

    @Autowired
    public IntTreadMgr intTreadMgr;

    public static void main(String[] args) {
        SpringApplication.run(TreadTestApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
         log.info("Application run!!");
    }
}
