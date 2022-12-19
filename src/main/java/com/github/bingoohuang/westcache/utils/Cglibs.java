package com.github.bingoohuang.westcache.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import java.lang.reflect.Proxy;

@UtilityClass
public class Cglibs {

    public String getSuperClassName(Object obj) {
        String name = obj.getClass().getName();
        int posCglib = name.indexOf("$$EnhancerByCGLIB$$");
        if (posCglib > 0) return name.substring(0, posCglib);

        int posSpring = name.indexOf("$$EnhancerBySpringCGLIB$$");
        if (posSpring > 0) return name.substring(0, posSpring);

        return name;
    }

    public Object proxy(Class<?> superClass,
                        MethodInterceptor interceptor,
                        Class<?>... interfaces) {
        return Enhancer.create(superClass, interfaces, interceptor);
    }

    public boolean isProxyClass(Class<?> targetClass) {
        val targetClassName = targetClass.getName();

        if (targetClassName.contains("CGLIB$$")) return true;
        return Proxy.isProxyClass(targetClass);
    }
}
