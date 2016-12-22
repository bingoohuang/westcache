package com.github.bingoohuang.westcache.utils;

import lombok.experimental.UtilityClass;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class CglibUtils {
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
}
