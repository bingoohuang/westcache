package com.github.bingoohuang.westcache.utils;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class Anns {
    public <T extends Annotation> T parseAnn(Method method, Class<T> annClz) {
        val methodAnn = method.getAnnotation(annClz);
        if (methodAnn != null) return methodAnn;

        val declaringClass = method.getDeclaringClass();
        val classAnn = declaringClass.getAnnotation(annClz);
        if (classAnn != null) return classAnn;

        Set<Annotation> setAnns = Sets.newHashSet();

        for (val ann : method.getAnnotations()) {
            val optionAnn = parseRecursiveAnn(setAnns, ann, annClz);
            if (optionAnn != null) return optionAnn;
        }

        for (val ann : declaringClass.getAnnotations()) {
            val optionAnn = parseRecursiveAnn(setAnns, ann, annClz);
            if (optionAnn != null) return optionAnn;
        }

        return null;
    }

    private <T extends Annotation> T parseRecursiveAnn(
            Set<Annotation> setAnns, Annotation ann, Class<T> annClz) {
        if (ann.toString().startsWith("@java.lang.")) return null;
        if (setAnns.contains(ann)) return null;

        setAnns.add(ann);

        val annotations = ann.annotationType().getAnnotations();
        for (val annotation : annotations) {
            if (annClz.isInstance(annotation)) return (T) annotation;
        }

        for (val annotation : annotations) {
            val option = parseRecursiveAnn(setAnns, annotation, annClz);
            if (option != null) return (T) annotation;
        }

        return null;
    }
}
