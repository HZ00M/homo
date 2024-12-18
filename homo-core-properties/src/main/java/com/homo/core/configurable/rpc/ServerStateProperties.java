package com.homo.core.configurable.rpc;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@Configurable
public class ServerStateProperties {
    /**
     * 本地内存用户连接信息保持时间(10分钟)
     */
    @Value("${homo.service.state.local.cache.duration.second:600}")
    private int localUserServicePodCacheSecond;
    /**
     * 服务器缓存用户连接信息保持时间(比连接短1分钟)
     */
    @Value("${homo.service.state.cache.duration.second:540}")
    private int remoteUserServicePodCacheSecond;

    /**
     * 服务器缓存用户连接信息延迟删除时间，默认60秒
     */
    @Value("${homo.service.state.cache.delay.remove.second:60}")
    private int remoteUserServicePodDelayRemoveSecond;

    /**
     * 服务器状态超时时间，默认60秒
     */
    @Value("${homo.service.state.expire.millSeconds:60000}")
    private long serviceStateExpireMillSeconds;

    /**
     * 服务器状态更新间隔
     */
    @Value("${homo.service.state.update.millSeconds:30000}")
    private int serviceStateUpdatePeriodMillSeconds;

    /**
     * 负载均衡因子 （0~1）0.5表示负载均衡0.5参考cpu，0.5参考内存值
     */
    @Value("${homo.service.state.cpu.factor:0.5}")
    private float cpuFactor;

    /**
     * 服务良好状态配置
     * Map<serviceName,goodState>
     */
    @Value("#{${homo.service.state.range:{}}}")
    private Map<String,Integer> goodStateRange = new HashMap<>();

    /**
     * 服务负载基数，提供一个缓冲值
     * Map<serviceName,goodState>
     */
    @Value("${homo.service.state.range.default:500}")
    private int defaultRange;
}
