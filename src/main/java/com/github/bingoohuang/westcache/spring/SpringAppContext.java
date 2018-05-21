package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.WestCacheRegistry;
import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.registry.RegistryTemplate;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/4.
 */
@Component
public class SpringAppContext implements ApplicationContextAware, SmartInitializingSingleton {
    private static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringAppContext.appContext = applicationContext;
    }

    @Override public void afterSingletonsInstantiated() {
        addSpringBeans(WestCacheConfig.class, WestCacheRegistry.REGISTRY_TEMPLATE);
        addSpringBeans(WestCacheFlusher.class, WestCacheRegistry.FLUSHER_REGISTRY);
        addSpringBeans(WestCacheManager.class, WestCacheRegistry.MANAGER_REGISTRY);
        addSpringBeans(WestCacheSnapshot.class, WestCacheRegistry.SNAPSHOT_REGISTRY);
        addSpringBeans(WestCacheKeyer.class, WestCacheRegistry.KEYER_REGISTRY);
        addSpringBeans(WestCacheInterceptor.class, WestCacheRegistry.INTERCEPTOR_REGISTRY);
    }

    public static ApplicationContext getAppContext() {
        return appContext;
    }

    public static <T> T inject(T bean) {
        appContext.getAutowireCapableBeanFactory().autowireBean(bean);
        return bean;
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

    public static <T> void addSpringBeans(Class<T> type, RegistryTemplate<T> registry) {
        val beans = appContext.getBeansOfType(type);

        for (val entry : beans.entrySet()) {
            registry.register(entry.getKey(), entry.getValue());
        }
    }

}
