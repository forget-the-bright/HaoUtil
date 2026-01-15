package org.hao.core.cache;


import org.hao.annotation.EnableAutoMethodCache;
import org.hao.core.cache.JetCacheProxyConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2026/1/14 16:44
 */
public class JetcacheConfigSelector extends AdviceModeImportSelector<EnableAutoMethodCache> {
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return this.getProxyImports();
            case ASPECTJ:
            default:
                return null;
        }
    }

    private String[] getProxyImports() {
        List<String> result = new ArrayList();
        result.add(AutoProxyRegistrar.class.getName());
        result.add(JetCacheProxyConfiguration.class.getName());
        return (String[]) result.toArray(new String[result.size()]);
    }
}
