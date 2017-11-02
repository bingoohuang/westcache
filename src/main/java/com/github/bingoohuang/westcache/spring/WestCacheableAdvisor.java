package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.utils.Anns;
import com.github.bingoohuang.westcache.utils.Envs;
import lombok.val;
import org.aopalliance.aop.Advice;
import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Component
public class WestCacheableAdvisor extends AbstractPointcutAdvisor {
    final transient StaticMethodMatcherPointcut pointcut =
            new StaticMethodMatcherPointcut() {
                @Override
                public ClassFilter getClassFilter() {
                    return new ClassFilter() {
                        @Override public boolean matches(Class<?> targetClass) {
                            if (targetClass.isInterface()) return false;

                            val targetClassName = targetClass.getName();
                            if (targetClassName.startsWith("com.sun.proxy.$Proxy")) return false;
                            if (targetClassName.startsWith("java.lang.")) return false;

                            if (Envs.classExists("org.n3r.eql.eqler.annotations.Eqler")) {
                                if (Anns.hasAnnotationInHierarchy(Eqler.class, targetClass)) return false;
                                if (Anns.hasAnnotationInHierarchy(EqlerConfig.class, targetClass)) return false;
                            }

                            return true;
                        }
                    };
                }

                @Override
                public boolean matches(Method method, Class<?> targetClass) {
                    if (method.isSynthetic()) return false;
                    if (method.isBridge()) return false;
                    if (method.getDeclaringClass() == Object.class) return false;
                    if (Modifier.isStatic(method.getModifiers())) return false;

                    return Anns.isFastWestCacheAnnotated(targetClass);
                }
            };

    @Autowired transient WestCacheableInterceptor interceptor;

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.interceptor;
    }
}

