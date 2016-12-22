package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.impl.WestCacheOption;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class CacheAnnotationUtils {
    public static WestCacheOption parseWestCacheableOption(Method method) {
        val westCacheable = method.getAnnotation(WestCacheable.class);
        if (westCacheable != null) return new WestCacheOption(westCacheable);

        for (val ann : method.getAnnotations()) {
            val optionAnn = getWestCacheableOption(ann);
            if (optionAnn != null) return new WestCacheOption(optionAnn);
        }

        return null;
    }

    private static WestCacheable getWestCacheableOption(Annotation ann) {
        val annotations = ann.annotationType().getAnnotations();
        for (val annotation : annotations) {
            if (annotation instanceof WestCacheable) {
                return (WestCacheable) annotation;
            }
        }

        for (val annotation : annotations) {
            val option = getWestCacheableOption(annotation);
            if (option != null) return (WestCacheable) annotation;
        }


        return null;
    }
}
