package org.hao.core.cache;


import org.hao.annotation.EnableAutoMethodCache;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

import java.util.ArrayList;
import java.util.List;

/**
 * JetCache配置选择器，用于根据不同的通知模式选择相应的导入配置
 * 继承自AdviceModeImportSelector，处理@EnableAutoMethodCache注解的配置导入
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 16:44
 */
public class JetcacheConfigSelector extends AdviceModeImportSelector<EnableAutoMethodCache> {
    /**
     * 根据通知模式选择需要导入的配置类
     *
     * @param adviceMode 通知模式，支持PROXY和ASPECTJ两种模式
     * @return 需要导入的配置类名称数组，如果模式不支持则返回null
     */
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                // 代理模式下返回代理相关的配置导入
                return this.getProxyImports();
            case ASPECTJ:
            default:
                // ASPECTJ模式暂不支持，返回null
                return null;
        }
    }

    /**
     * 获取代理模式下的配置导入类
     *
     * @return 代理模式下需要导入的配置类名称数组
     */
    private String[] getProxyImports() {
        List<String> result = new ArrayList();
        // 添加自动代理注册器
        result.add(AutoProxyRegistrar.class.getName());
        // 添加JetCache代理配置
        result.add(JetCacheProxyConfiguration.class.getName());
        return (String[]) result.toArray(new String[result.size()]);
    }
}
