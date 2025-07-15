package org.hao.core.ip;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.hao.core.thread.ThreadUtil;
import org.hao.vo.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);
    public static List<String> allIP;
    public static List<String> localAddresses;

    static {
        Tuple<List<String>, List<String>> addressTuple = getAllIP();
        allIP = addressTuple.getFirst();
        localAddresses = addressTuple.getSecond();
    }

    private IPUtils() {
    }


    public static String getLocalIP() {
        if (CollUtil.isNotEmpty(localAddresses)) {
            return localAddresses.get(0);
        }
        return allIP.get(0);
    }

    private static Tuple<List<String>, List<String>> getAllIP() {
        List<String> ipAddresses = new ArrayList<>();
        List<String> localAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() &&
                            inetAddress instanceof java.net.Inet4Address
                    ) {
                        ipAddresses.add(inetAddress.getHostAddress());
                    }

                    String name = networkInterface.getDisplayName();
                    // 常见的虚拟网卡或容器相关接口，按名字过滤
                    if (!(name.contains("Virtual") ||
                            name.contains("VPN") ||
                            StrUtil.startWithIgnoreCase(name, "br-") ||
                            StrUtil.startWithIgnoreCase(name, "tun") ||
                            name.contains("Tunnel") ||
                            name.contains("Adapter") ||
                            name.contains("Sangfor")) &&
                            !inetAddress.isLoopbackAddress() &&
                            inetAddress instanceof java.net.Inet4Address) {
                        localAddresses.add(inetAddress.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("获取IP地址异常", e);
        }
        ipAddresses.sort(Comparator.comparing(String::toString));
        return Tuple.newTuple(ipAddresses, localAddresses);
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;

        try {
            ip = request.getHeader("x-forwarded-for");
            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }

            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }

            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }

            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }

            if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception var3) {
            logger.error("IPUtils ERROR ", var3);
        }

        return ip;
    }

    /**
     * 获取基础URL
     * 该方法用于动态生成当前应用的基URL，考虑了HTTP和HTTPS以及不同服务器端口的情况
     * 主要用途包括生成绝对URL路径，例如在发送邮件、生成支付签名等场景中需要完整的URL时使用
     *
     * @return 当前应用的基础URL字符串
     */
    public static String getBaseUrl() {
        // 获取当前线程关联的HTTP请求对象
        HttpServletRequest request = ThreadUtil.getRequest();

        // 尝试从请求头中获取实际使用的方案（如http、https），以支持负载均衡等场景
        String scheme = request.getHeader("X-Forwarded-Scheme");
        // 如果请求头中未提供方案，则使用请求直接提供的方案
        if (StrUtil.isEmpty(scheme)) {
            scheme = request.getScheme();
        }

        // 获取服务器名称，即域名或IP地址
        String serverName = request.getServerName();
        // 获取服务器端口

        // 返回构造的基础URL
        return getString(request, scheme, serverName);
    }

    private static String getString(HttpServletRequest request, String scheme, String serverName) {
        int serverPort = request.getServerPort();
        // 获取应用的上下文路径
        String contextPath = request.getContextPath();

        // 初始化基础URL变量
        String baseDomainPath;
        // 定义标准HTTP端口
        int httpPort = 80;
        // 根据服务器端口是否为标准HTTP端口，构造基础URL
        if (httpPort == serverPort) {
            // 如果是标准端口，URL中不显示端口号
            baseDomainPath = scheme + "://" + serverName + contextPath;
        } else {
            // 如果不是标准端口，URL中包含端口号
            baseDomainPath = scheme + "://" + serverName + ":" + serverPort + contextPath;
        }

        // 返回构造的基础URL
        return baseDomainPath;
    }

}

