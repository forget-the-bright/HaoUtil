package org.hao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置属性类，用于映射 `application.yml` 中以 {@code hao-util} 为前缀的配置项。
 *
 * <p>该类定义了 HaoUtil 工具模块的可配置参数，包括是否启用日志切面、接口打印、WebSocket 支持等。</p>
 *
 * @author wanghao (helloworlwh@163.com)
 * @since 2024/10/30
 */

@ConfigurationProperties(prefix = "hao-util")
public class HaoUtilProperties {
    private boolean enabled;
    private boolean printInterface;
    private boolean enableWs = false;
    private int wsSchedulerPoolSize = 1000;

    public boolean isPrintInterface() {
        return printInterface;
    }

    public void setPrintInterface(boolean printInterface) {
        this.printInterface = printInterface;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWsSchedulerPoolSize() {
        return wsSchedulerPoolSize;
    }

    public void setWsSchedulerPoolSize(int wsSchedulerPoolSize) {
        this.wsSchedulerPoolSize = wsSchedulerPoolSize;
    }

    public boolean isEnableWs() {
        return enableWs;
    }

    public void setEnableWs(boolean enableWs) {
        this.enableWs = enableWs;
    }
}
