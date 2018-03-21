package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.utils.Anns;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class WestCacheStaticMethodMatcherPointcut extends StaticMethodMatcherPointcut {
    @Override
    public ClassFilter getClassFilter() {
        return new WestCacheClassFilter();
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.isSynthetic()) return false;
        if (method.isBridge()) return false;
        if (method.getDeclaringClass() == Object.class) return false;
        if (Modifier.isStatic(method.getModifiers())) return false;


        return Anns.isFastWestCacheAnnotated(method);
    }
}
