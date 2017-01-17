package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.WestCacheRegistry;
import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.registry.RegistryTemplate;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/4.
 */
@Component
public class SpringAppContext implements ApplicationContextAware,
        ApplicationListener<ContextRefreshedEvent> {
    private static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringAppContext.appContext = applicationContext;
    }

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        addSpringBeans(WestCacheConfig.class, WestCacheRegistry.configRegistry);
        addSpringBeans(WestCacheFlusher.class, WestCacheRegistry.flusherRegistry);
        addSpringBeans(WestCacheManager.class, WestCacheRegistry.managerRegistry);
        addSpringBeans(WestCacheSnapshot.class, WestCacheRegistry.snapshotRegistry);
        addSpringBeans(WestCacheKeyer.class, WestCacheRegistry.keyerRegistry);
        addSpringBeans(WestCacheInterceptor.class, WestCacheRegistry.interceptorRegistry);
    }

    public static ApplicationContext getAppContext() {
        return appContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        if (appContext == null) return null;
        if (StringUtils.isEmpty(beanName)) return null;

        try {
            return (T) appContext.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            // ignore
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) {
        if (appContext == null) return null;
        if (clazz == null) return null;

        try {
            return appContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            // ignore
        }

        return null;
    }

    public <T> void addSpringBeans(Class<T> type, RegistryTemplate<T> registry) {
        val beans = appContext.getBeansOfType(type);

        for (val entry : beans.entrySet()) {
            registry.register(entry.getKey(), entry.getValue());
        }
    }

}
