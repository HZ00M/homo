package com.homo.core.rpc.base.utils;

import com.homo.core.facade.service.ServiceExport;
import com.homo.core.utils.reflect.HomoAnnotationUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ServiceUtil {
    /**
     * 通过服务名，获得域名
     *
     * @param serviceTag 服务名字
     * @return
     */
    public String getServiceHostNameByTag(String serviceTag) {
        String[] stringArray = serviceTag.split(":");
        return stringArray[0];
    }

    /**
     * 通过服务名，获得端口
     *
     * @param serviceTag 服务名字
     * @return
     */
    public int getServicePortByTag(String serviceTag) {
        String[] stringArray = serviceTag.split(":");
        return Integer.parseInt(stringArray[stringArray.length - 1]);
    }


    /**
     * 通过服务名，获得端口
     *
     * @param realHost 服务名字
     * @return
     */
    public int getServicePortByRealHost(String realHost) {
        String[] stringArray = realHost.split(":");
        return Integer.parseInt(stringArray[stringArray.length - 1]);
    }

    /**
     * 通过服务名，获得域名
     *
     * @param realHost 服务名字
     * @return
     */
    public String getServiceHostNameByRealHost(String realHost) {
        String[] stringArray = realHost.split(":");
        return stringArray[0];
    }

    /**
     * 根据服务子类信息获取服务名字
     *
     * @param selfZz 服务子类，不能是接口类！！！
     * @return 服务名字
     */
    public static String getServiceTagName(Class<?> selfZz) {
        ServiceExport serviceExport = HomoAnnotationUtil.findAnnotation(selfZz, ServiceExport.class);
        if (serviceExport != null) {
            return serviceExport.tagName();
        }
        return null;
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

        String statefulHostName = serviceHost + "-" + podIndex + "." + serviceHost + ":" + port;
//        String statefulName = String.format("%s-%d.%s:%d", serviceHost, podIndex, serviceHost, port);
        log.debug("statefulName : {}", statefulHostName);
        return statefulHostName;
    }

    public static boolean isStatefulService(String realHostName) {
        return realHostName.contains("-")&&realHostName.contains(".");//todo 待验证
    }
}
