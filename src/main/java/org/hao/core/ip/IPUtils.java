package org.hao.core.ip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * IP 地址工具类，提供本地 IP 获取、所有网卡 IP 收集及 HTTP 请求客户端 IP 识别功能。
 *
 * <p>该类封装了从不同网络接口获取 IP 地址的方法，并支持通过请求头信息提取客户端真实 IP，
 * 常用于日志记录、权限控制、服务注册等场景。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/5/30
 */

public class IPUtils {
    private static Logger logger = LoggerFactory.getLogger(IPUtils.class);
    public static List<String> allIP = new ArrayList<>();

    static {
        allIP = getAllIP();
    }

    public IPUtils() {
    }


    public static String getLocalIP() {

        try {
            Class.forName("org.springframework.cloud.commons.util.InetUtilsProperties");
            InetUtilsProperties inetUtilsProperties = SpringUtil.getBean(InetUtilsProperties.class);
            List<String> preferredNetworks = inetUtilsProperties.getPreferredNetworks();
            if (allIP.isEmpty()) return "127.0.0.1";
            if (CollUtil.isEmpty(preferredNetworks)) return allIP.get(0);
            for (String ip : allIP) {
                for (String preferredNetwork : preferredNetworks) {
                    if (ip.startsWith(preferredNetwork)) {
                        return ip;
                    }
                }
            }

        } catch (ClassNotFoundException  e) {

        }
        return   allIP.get(0);
        //InetAddress.getLocalHost().getHostAddress()
    }

    private static List<String> getAllIP() {
        List<String> ipAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof InetAddress && inetAddress instanceof java.net.Inet4Address) {
                        ipAddresses.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ipAddresses.sort(Comparator.comparing(String::toString));
        return ipAddresses;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;

        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }

            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception var3) {
            logger.error("IPUtils ERROR ", var3);
        }

        return ip;
    }
}

