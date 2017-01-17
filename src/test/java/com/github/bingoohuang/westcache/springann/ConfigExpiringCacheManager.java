package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.manager.ExpiringMapCacheManager.ExpiringMapCache;
import com.github.bingoohuang.westcache.utils.Config;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.val;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.jodah.expiringmap.ExpirationPolicy.CREATED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/17.
 */
@Component("configExpiring")
public class ConfigExpiringCacheManager extends BaseCacheManager {
    public ConfigExpiringCacheManager() {
        super(new ConfigExpiringMapCache());
    }

    public static class ConfigExpiringMapCache extends ExpiringMapCache {
        @Override
        protected void putItem(WestCacheOption option,
                               String cacheKey,
                               WestCacheItem cacheItem) {
            val configKey = "ttlConfigKey";
            val ttlConfigKey = option.getSpecs().get(configKey);
            if (isNotBlank(ttlConfigKey)) {
                val ttlConfig = Config.getConfig(ttlConfigKey);
                val duration = Durations.parse(configKey, ttlConfig, SECONDS);
                cache.put(cacheKey, cacheItem, CREATED, duration, SECONDS);
            } else {
                cache.put(cacheKey, cacheItem);
            }
        }
    }
}
