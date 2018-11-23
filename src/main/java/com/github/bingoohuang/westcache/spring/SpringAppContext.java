package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.utils.spring.SpringContext;
import com.github.bingoohuang.westcache.WestCacheRegistry;
import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.registry.RegistryTemplate;
import lombok.val;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/4.
 */
@Component
public class SpringAppContext extends SpringContext implements ApplicationContextAware, SmartInitializingSingleton {
    @Override public void afterSingletonsInstantiated() {
        addSpringBeans(WestCacheConfig.class, WestCacheRegistry.REGISTRY_TEMPLATE);
        addSpringBeans(WestCacheFlusher.class, WestCacheRegistry.FLUSHER_REGISTRY);
        addSpringBeans(WestCacheManager.class, WestCacheRegistry.MANAGER_REGISTRY);
        addSpringBeans(WestCacheSnapshot.class, WestCacheRegistry.SNAPSHOT_REGISTRY);
        addSpringBeans(WestCacheKeyer.class, WestCacheRegistry.KEYER_REGISTRY);
        addSpringBeans(WestCacheInterceptor.class, WestCacheRegistry.INTERCEPTOR_REGISTRY);
    }

    private static <T> void addSpringBeans(Class<T> type, RegistryTemplate<T> registry) {
        val beans = getAppContext().getBeansOfType(type);

        for (val entry : beans.entrySet()) {
            registry.register(entry.getKey(), entry.getValue());
        }
    }

}
