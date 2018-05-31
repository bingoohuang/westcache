package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.bingoohuang.westcache.utils.Envs;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Set;

@Slf4j
public class WestCacheableClassPathScanner extends ClassPathBeanDefinitionScanner {
    public WestCacheableClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * Configures parent scanner to search for the right interfaces. It can search
     * for all interfaces or just for those that extends a markerInterface or/and
     * those annotated with the annotationClass
     */
    public void registerFilters() {
        if (Envs.classExists("org.n3r.eql.eqler.annotations.Eqler")) {
            addExcludeFilter(new AnnotationTypeFilter(Eqler.class));
            addExcludeFilter(new AnnotationTypeFilter(EqlerConfig.class));
        }

        addIncludeFilter((metadataReader, metadataReaderFactory) -> {
            val metadata = metadataReader.getClassMetadata();
            if (!metadata.isInterface()) return false;

            val className = metadata.getClassName();
            val clazz = Envs.forName(className);
            return Anns.isFastWestCacheAnnotated(clazz);
        });

    }

    /**
     * Calls the parent search that will search and register all the candidates.
     * Then the registered objects are post processed to set them as
     * MapperFactoryBeans
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            log.warn("No WestCacheable was found in '{}' package. " +
                            "Please check your configuration.",
                    Arrays.toString(basePackages));
            return beanDefinitions;
        }

        for (val holder : beanDefinitions) {
            val definition = (GenericBeanDefinition) holder.getBeanDefinition();
            log.debug("Creating WestCacheableFactoryBean with name '{}' and '{}'",
                    holder.getBeanName(), definition.getBeanClassName());

            // the mapper interface is the original class of the bean
            // but, the actual class of the bean is MapperFactoryBean
            definition.getPropertyValues().add("targetClass", definition.getBeanClassName());
            definition.setBeanClass(WestCacheableFactoryBean.class);
        }

        return beanDefinitions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(
            AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface()
                && beanDefinition.getMetadata().isIndependent();
    }


    public static class WestCacheableFactoryBean<T> implements FactoryBean<T> {
        @Setter private Class<T> targetClass;

        @Override
        public T getObject() {
            return WestCacheFactory.create(targetClass);
        }

        @Override
        public Class<?> getObjectType() {
            return this.targetClass;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }

}
