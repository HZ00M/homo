package com.homo.core.rpc.base;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ServiceUtil {
    /**
     * 通过服务名，获得域名
     *
     * @param serviceName 服务名字
     * @return
     */
    public String getServiceHostName(String serviceName) {
        String[] stringArray = serviceName.split(":");
        return stringArray[0];
    }

    /**
     * 通过服务名，获得端口
     *
     * @param serviceName 服务名字
     * @return
     */
    public int getServicePort(String serviceName) {
        String[] stringArray = serviceName.split(":");
        return Integer.parseInt(stringArray[stringArray.length - 1]);
    }

    /**
     * 格式化有状态服务访问名称
     * @param tagName 服务名
     * @param podIndex pod index
     * @return 状态服务访问名称
     */
    public String formatStatefulName(String tagName, Integer podIndex) {
        //命名格式: [tagName]:[port]
        String[] stringArray = tagName.split(":");
        String serviceHost = stringArray[0];
        Integer port = Integer.parseInt(stringArray[stringArray.length - 1]);

        String name = serviceHost + "-" + podIndex + "." + serviceHost + ":" + port;
//        String statefulName = String.format("%s-%d.%s:%d", serviceHost, podIndex, serviceHost, port);
        log.debug("statefulName : {}", name);
        return name;
    }
}
