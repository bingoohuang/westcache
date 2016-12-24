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
     *
     * @param obj cglib enhanced object.
     * @return underlined super class's name
     */
    public String getSuperClassName(Object obj) {
        String name = obj.getClass().getName();
        int pos = name.indexOf("$$EnhancerByCGLIB$$");

        return pos < 0 ? name : name.substring(0, pos);
    }

    public Object proxy(Class<?> targetClass,
                        MethodInterceptor interceptor,
                        Class<?>... interfaces) {
        return Enhancer.create(targetClass, interfaces, interceptor);
    }
}
