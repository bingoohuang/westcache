package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.manager.ExpiringMapCacheManager.ExpiringCache;
import com.github.bingoohuang.westcache.utils.Config;
import com.github.bingoohuang.westcache.utils.Durations;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.val;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static net.jodah.expiringmap.ExpirationPolicy.CREATED;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/17.
 */
@Component("configExpiring")
public class ConfigExpiringManager extends BaseCacheManager {
    public ConfigExpiringManager() {
        super(new ConfigExpiringCache());
    }

    public static class ConfigExpiringCache extends ExpiringCache {
        @Override
        protected void putItem(WestCacheOption option,
                               String cacheKey,
                               WestCacheItem item) {
            val configKey = "ttlConfigKey";
            val ttlConfigKey = option.getSpecs().get(configKey);
            if (isBlank(ttlConfigKey)) {
                cache.put(cacheKey, item);
                return;
            }

            val ttlConfig = Config.getConfig(ttlConfigKey);
            val duration = Durations.parse(configKey, ttlConfig);
            cache.put(cacheKey, item, CREATED, duration, TimeUnit.SECONDS);
        }
    }
}
