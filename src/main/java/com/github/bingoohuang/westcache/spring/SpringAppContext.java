package com.github.bingoohuang.westcache.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/4.
 */
@Component
public class SpringAppContext implements ApplicationContextAware {
    private static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringAppContext.appContext = applicationContext;
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
}
