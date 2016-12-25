package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class WestCacheAnns {
    public WestCacheable parseWestCacheable(Method method) {
        val westCacheable = method.getAnnotation(WestCacheable.class);
        if (westCacheable != null) return westCacheable;

        for (val ann : method.getAnnotations()) {
            val optionAnn = parseRecursiveWestCacheable(ann);
            if (optionAnn != null) return optionAnn;
        }

        return null;
    }

    private WestCacheable parseRecursiveWestCacheable(Annotation ann) {
        val annotations = ann.annotationType().getAnnotations();
        for (val annotation : annotations) {
            if (annotation instanceof WestCacheable) {
                return (WestCacheable) annotation;
            }
        }

        for (val annotation : annotations) {
            val option = parseRecursiveWestCacheable(annotation);
            if (option != null) return (WestCacheable) annotation;
        }

        return null;
    }
}
