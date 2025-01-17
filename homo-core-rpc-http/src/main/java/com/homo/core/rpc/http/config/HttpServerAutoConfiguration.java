package com.homo.core.rpc.http.config;

import com.homo.core.configurable.rpc.RpcHttpServerProperties;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.rpc.http.HttpRpcServerFactory;
import com.homo.core.rpc.http.MockWebServer;
import com.homo.core.rpc.http.HttpRpcClientFactory;
import com.homo.core.rpc.http.exception.HomoHttpExceptionHandler;
import com.homo.core.rpc.http.filter.CorsFilter;
import com.homo.core.rpc.http.filter.TraceFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
@Slf4j
@Import(RpcHttpServerProperties.class)
@EnableWebFlux//必须配置这个以开启webflux自动化配置
//@ComponentScan(excludeFilters = {@ComponentScan.Filter(type =
//        FilterType.ASSIGNABLE_TYPE, classes = {NettyReactiveWebServerFactory.class})})
public class HttpServerAutoConfiguration {
    @Bean("corsFilter")
    public WebFilter corsFilter(){
        log.info("register bean corsFilter");
        return new CorsFilter();
    }

    @Bean("traceFilter")
    public WebFilter traceFilter(){
        log.info("register bean traceFilter");
        return new TraceFilter();
    }

    @Bean("exceptionHandler")
    public HomoHttpExceptionHandler exceptionHandler(){
        log.info("register bean exceptionHandler");
        return new HomoHttpExceptionHandler();
    }

    @Bean("httpRpcServerFactory")
    public RpcServerFactory httpRpcServerFactory(){
        log.info("register bean httpRpcServerFactory");
        return new HttpRpcServerFactory();
    }

    @Bean("httpRpcClientFactory")
    public RpcClientFactory httpRpcClientFactory(){
        log.info("register bean httpRpcClientFactory");
        return new HttpRpcClientFactory();
    }

    /**
     * 配置成不占用端口的方式启动
     * mock一个httpServer，禁用 webflux 默认启动的 netty-server\
     * 需要在application.properties中添加spring.main.web-application-type=none， 如果没有添加就会抛出异常
     */
    @ConditionalOnClass(DispatcherHandler.class)
    @Bean
    @Primary
    public ReactiveWebServerFactory webServerFactory(){
        return httpHandler -> new MockWebServer();
    }

}
