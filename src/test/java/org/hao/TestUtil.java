package org.hao;

import lombok.extern.slf4j.Slf4j;
import org.hao.core.cache.LocalTimedCacheHolder;
import org.junit.jupiter.api.Test;

/**
 * TODO
 *
 * @author wanghao(helloworlwh@163.com)
 * @since 2025/12/24 13:45
 */
@Slf4j
public class TestUtil {
    @Test
    public void testLocalCache() throws Exception {
        LocalTimedCacheHolder<String, String> localTimedCacheHolder = new LocalTimedCacheHolder<>(5);
        localTimedCacheHolder.put("1", "123");
        log.info("1:{}", localTimedCacheHolder.get("1"));
        Thread.sleep(6000);
        log.info("1:{}", localTimedCacheHolder.get("1"));
    }
}
