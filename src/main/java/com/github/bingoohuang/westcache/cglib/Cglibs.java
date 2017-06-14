package com.github.bingoohuang.westcache.cglib;

import lombok.experimental.UtilityClass;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class Cglibs {
    /**
     * proxied object class will be like
     * com.github.bingoohuang.westcache.RefreshTest$FlushBean$$EnhancerByCGLIB$$8b0b1c89
     * com.github.bingoohuang.westcache.springdemo.CacheInterfaceDemo$$EnhancerBySpringCGLIB$$5669a142
     *
     * @param obj cglib enhanced object.
     * @return underlined super class's name
     */
    public static String getSuperClassName(Object obj) {
        String name = obj.getClass().getName();
        int posCglib = name.indexOf("$$EnhancerByCGLIB$$");
        if (posCglib > 0) return name.substring(0, posCglib);

        int posSpring = name.indexOf("$$EnhancerBySpringCGLIB$$");
        if (posSpring > 0) return name.substring(0, posSpring);

        return name;
    }

    /**
     * create a proxied object by cglib.
     *
     * @param superClass  target class.
     * @param interceptor interceptor.
     * @param interfaces  interfaces.
     * @return proxied object.
     */
    public static Object proxy(Class<?> superClass,
                               MethodInterceptor interceptor,
                               Class<?>... interfaces) {
        return Enhancer.create(superClass, interfaces, interceptor);
    }
}
