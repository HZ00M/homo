package com.homo.core.utils.origin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;


/**
 * Copyright (c) 2016 OPPO, All Rights Reserved.
 * <p>
 * Created by houzhenjing on 2017-05-03.
 */
public abstract class NetUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);
    
    private static final String UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST";
    
    private static final String UNKNOWN_IP = "UNKNOWN_IP";
    
    public static String getLocalHostName() {
        try {
            final InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (final UnknownHostException uhe) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nic = interfaces.nextElement();
                    final Enumeration<InetAddress> addresses = nic.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        final InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress()) {
                            final String hostname = address.getHostName();
                            if (hostname != null) {
                                return hostname;
                            }
                        }
                    }
                }
            } catch (final SocketException se) {
                LOGGER.error("Could not determine local host name", uhe);
                return UNKNOWN_LOCALHOST;
            }
            LOGGER.error("Could not determine local host name", uhe);
            return UNKNOWN_LOCALHOST;
        }
    }
    
    public static String getIp() {
        try {
            return (InetAddress.getLocalHost()).getHostAddress();
        } catch (UnknownHostException e) {
            return UNKNOWN_IP;
        }
    }
    
    /**
     * 判断本地能否访问远程的IP和Port
     *
     * @param remoteIp 目标ip
     * @param remotePort 目标port
     * @param timeout 超时时间
     */
    public static boolean isReachable(String remoteIp, int remotePort, int timeout) {
        
        boolean isReachable = false;
        
        try (Socket socket = new Socket()) {
            InetAddress localInetAddr = InetAddress.getLocalHost();
            InetAddress remoteInetAddr = InetAddress.getByName(remoteIp);
            // 端口号设置为 0 表示在本地挑选一个可用端口进行连接
            SocketAddress localSocketAddr = new InetSocketAddress(localInetAddr, 0);
            socket.bind(localSocketAddr);
            InetSocketAddress endpointSocketAddr =
                new InetSocketAddress(remoteInetAddr, remotePort);
            socket.connect(endpointSocketAddr, timeout);
            LOGGER.info("SUCCESS - connection established! Local: " +
                localInetAddr.getHostAddress() + " remote: " +
                remoteInetAddr.getHostAddress() + " port: " + remotePort);
            isReachable = true;
        } catch (Exception e) {
            LOGGER.error("FAILRE - CAN not connect! remote: " +
                remoteIp + " port" + remotePort);
        }
        return isReachable;
    }
    
    
}
