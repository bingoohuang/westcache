package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

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
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader,
                                 MetadataReaderFactory metadataReaderFactory) throws IOException {
                return !metadataReader.getClassMetadata().isInterface();
            }
        });
        addIncludeFilter(new TypeFilter() {
            @Override @SneakyThrows
            public boolean match(MetadataReader metadataReader,
                                 MetadataReaderFactory metadataReaderFactory
            ) throws IOException {
                val classMetadata = metadataReader.getClassMetadata();
                val className = classMetadata.getClassName();
                Class c = Class.forName(className);

                for (Method m : c.getMethods()) {
                    val option = WestCacheOption.parseWestCacheable(m);
                    if (option == null) return false;
                }

                return true;
            }
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
            logger.warn("No WestCacheable was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            for (BeanDefinitionHolder holder : beanDefinitions) {
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

                if (logger.isDebugEnabled()) {
                    logger.debug("Creating WestCacheableFactoryBean with name '" + holder.getBeanName()
                            + "' and '" + definition.getBeanClassName() + "' eqlerInterface");
                }

                // the mapper interface is the original class of the bean
                // but, the actual class of the bean is MapperFactoryBean
                definition.getPropertyValues().add("targetClass", definition.getBeanClassName());
                definition.setBeanClass(WestCacheableFactoryBean.class);
            }
        }

        return beanDefinitions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if (super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            logger.warn("Skipping WestCacheableFactoryBean with name '" + beanName
                    + "' and '" + beanDefinition.getBeanClassName() + "' targetClass"
                    + ". Bean already defined with the same name!");
            return false;
        }
    }


    public static class WestCacheableFactoryBean<T> implements FactoryBean<T> {
        @Setter private Class<T> targetClass;

        @Override
        public T getObject() throws Exception {
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
