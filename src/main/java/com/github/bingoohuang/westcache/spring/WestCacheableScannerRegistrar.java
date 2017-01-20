package com.github.bingoohuang.westcache.spring;

import lombok.Setter;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WestCacheableScannerRegistrar
        implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    @Setter private ResourceLoader resourceLoader;

    /**
     * {@inheritDoc}
     */
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {
        val name = WestCacheableScan.class.getName();
        val attributes = metadata.getAnnotationAttributes(name);
        val annoAttrs = AnnotationAttributes.fromMap(attributes);
        val scanner = new WestCacheableClassPathScanner(registry);
        setScannerResLoader(scanner);
        setBeanNameGenerator(annoAttrs, scanner);
        String[] basePackages = addBasePakcages(metadata, annoAttrs);

        scanner.registerFilters();
        scanner.doScan(basePackages);
    }

    private void setScannerResLoader(WestCacheableClassPathScanner scanner) {
        // this check is needed in Spring 3.1
        if (resourceLoader != null) scanner.setResourceLoader(resourceLoader);
    }

    private void setBeanNameGenerator(AnnotationAttributes annoAttrs,
                                      WestCacheableClassPathScanner scanner) {
        val generatorClass = annoAttrs.getClass("nameGenerator");
        if (BeanNameGenerator.class.equals(generatorClass)) return;

        val generator = BeanUtils.instantiateClass(generatorClass);
        scanner.setBeanNameGenerator((BeanNameGenerator) generator);
    }

    private String[] addBasePakcages(AnnotationMetadata metadata,
                                     AnnotationAttributes attrs) {
        List<String> basePkgs = new ArrayList<String>();
        addBasePackages(attrs, basePkgs, "value");
        addBasePackages(attrs, basePkgs, "basePackages");
        for (val clz : attrs.getClassArray("basePackageClasses")) {
            basePkgs.add(ClassUtils.getPackageName(clz));
        }

        if (basePkgs.isEmpty()) {
            basePkgs.add(ClassUtils.getPackageName(metadata.getClassName()));
        }

        return StringUtils.toStringArray(basePkgs);
    }

    private void addBasePackages(AnnotationAttributes annoAttrs,
                                 List<String> basePackages,
                                 String attrName) {
        for (val pkg : annoAttrs.getStringArray(attrName)) {
            if (StringUtils.hasText(pkg)) basePackages.add(pkg);
        }
    }
}
