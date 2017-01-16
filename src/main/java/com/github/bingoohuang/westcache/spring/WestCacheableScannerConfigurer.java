package com.github.bingoohuang.westcache.spring;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import static org.springframework.util.Assert.notNull;

public class WestCacheableScannerConfigurer
        implements BeanDefinitionRegistryPostProcessor,
        InitializingBean, ApplicationContextAware, BeanNameAware {

    @Setter private String basePackage;
    @Setter private ApplicationContext applicationContext;
    @Setter private String beanName;
    @Getter @Setter private BeanNameGenerator nameGenerator;

    public void afterPropertiesSet() throws Exception {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // left intentionally blank
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        val scanner = new WestCacheableClassPathScanner(registry);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }
}

