package org.hao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description TODO
 * Author wanghao(helloworlwh @ 163.com)
 * Date 2024/10/30 上午9:54
 */
@ConfigurationProperties(prefix = "hao-util")
public class HaoUtilProperties {
    private boolean enabled;
    private boolean printInterface;

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
}
