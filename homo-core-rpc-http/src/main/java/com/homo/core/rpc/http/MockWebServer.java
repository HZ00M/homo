package com.homo.core.rpc.http;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

public class MockWebServer implements WebServer {
    @Override
    public void start() throws WebServerException {
        // 禁用默认的werflux server的启动
        throw new WebServerException("spring.main.web-application-type=none", new Throwable());
    }

    @Override
    public void stop() throws WebServerException {
        // 禁用默认的werflux server的停止
        throw new WebServerException("spring.main.web-application-type=none", new Throwable());
    }

    @Override
    public int getPort() {
        return 0;
    }
}
