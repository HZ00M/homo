package com.homo.core.rpc.http;

import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceInfo;
import com.homo.core.utils.module.Module;
import com.homo.core.utils.module.RootModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class HttpRpcClientFactory implements RpcClientFactory, Module {
    @Autowired
    private RootModule rootModule;
    @Override
    public RpcAgentClient newAgent(String hostname, ServiceInfo serviceInfo) {
        WebClient webClient = WebClient.builder()
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest()); // 打印请求日志
                    exchangeFilterFunctions.add(logResponse()); // 打印响应日志
                })
                .build();
        RpcAgentClient agentClient = new HttpRpcAgentClient(rootModule.getServerInfo().serverName, hostname,serviceInfo.getServerPort(),
                webClient, rootModule.getServerInfo().isStateful, serviceInfo.isStateful());
        return agentClient;
    }

    @Override
    public RpcType getType() {
        return RpcType.http;
    }

    // 自定义日志打印函数
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("RequestProcessor method {} uri {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                    log.info("RequestProcessor header {}", name + ": " + String.join(",", values))
            );
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    log.info("ResponseProcessor header {}", name + ": " + String.join(",", values))
            );
            return Mono.just(clientResponse);
        });
    }
}
