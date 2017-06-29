package com.github.bingoohuang.westcache.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@UtilityClass
public class Anns {

    public static final String SPECS = "specs";

    static Map<String, String> parseWestCacheable(
            Method method,
            Class<? extends Annotation> annClass) {
        val methodAnn = method.getAnnotation(annClass);
        val declaringClz = method.getDeclaringClass();
        val classAnn = declaringClz.getAnnotation(annClass);

        if (methodAnn != null || classAnn != null) {
            val methodAttrs = getAllAttrs(methodAnn);
            val classAttrs = getAllAttrs(classAnn);
            return mergeMap(classAttrs, methodAttrs);
        }

        Set<Annotation> setAnns = Sets.newHashSet();
        val annM = searchAnn(setAnns, method.getAnnotations(), annClass);
        val annC = searchAnn(setAnns, declaringClz.getAnnotations(), annClass);
        if (annM != null || annC != null) {
            return mergeMap(annC, annM);
        }

        return null;
    }

    private static Map<String, String> mergeMap(
            Map<String, String> classAttrs,
            Map<String, String> methodAttrs) {
        Map<String, String> attrs = Maps.newHashMap();
        if (classAttrs != null) merge(attrs, classAttrs);
        if (methodAttrs != null) merge(attrs, methodAttrs);

        return attrs;
    }

    private static Map<String, String> searchAnn(
            Set<Annotation> setAnns,
            Annotation[] anns,
            Class<? extends Annotation> annClass) {
        for (val ann : anns) {
            val optionAnn = parseRecursiveAnn(setAnns, ann, annClass);
            if (optionAnn != null) return optionAnn;
        }

        return null;
    }

    public static Map<String, String> removeAttrs(
            Map<String, String> map, String... attrs) {
        for (String attr : attrs) {
            map.remove(attr);
        }

        return map;
    }

    public static Map<String, String> getAllAttrs(Annotation ann) {
        Map<String, String> attrs = Maps.newHashMap();
        if (ann == null) return attrs;

        for (val method : ann.annotationType().getDeclaredMethods()) {
            Object attr = Envs.invoke(method, ann);
            String value = String.valueOf(attr);
            if (StringUtils.isNotEmpty(value)) {
                attrs.put(method.getName(), value);
            }
        }
        return attrs;
    }

    private static Map<String, String> parseRecursiveAnn(
            Set<Annotation> setAnns,
            Annotation ann,
            Class<?> annClz) {
        if (ann.toString().startsWith("@java.lang.")) return null;
        if (setAnns.contains(ann)) return null;

        setAnns.add(ann);

        val annotations = ann.annotationType().getAnnotations();
        val annAttrs = checkCurrentAnn(ann, annClz, annotations);
        if (annAttrs != null) return annAttrs;

        return checkInheritAnn(setAnns, ann, annClz, annotations);
    }

    private static Map<String, String> checkInheritAnn(
            Set<Annotation> setAnns,
            Annotation ann,
            Class<?> annClz,
            Annotation[] annotations) {
        for (val annotation : annotations) {
            val attrs = parseRecursiveAnn(setAnns, annotation, annClz);
            if (attrs == null) continue;

            val thisAttrs = getAllAttrs(ann);
            return merge(attrs, thisAttrs);
        }

        return null;
    }

    private static Map<String, String> checkCurrentAnn(
            Annotation ann,
            Class<?> annClz,
            Annotation[] annotations) {
        for (val typeAnn : annotations) {
            if (!annClz.isInstance(typeAnn)) continue;

            val annAttrs = getAllAttrs(typeAnn);
            val thisAttrs = getAllAttrs(ann);
            return merge(annAttrs, thisAttrs);
        }
        return null;
    }

    static Joiner.MapJoiner mapJoiner = Joiner.on(';').withKeyValueSeparator('=');

    private static Map<String, String> merge(Map<String, String> firstMap,
                                             Map<String, String> otherMap) {
        String specs1 = firstMap.get(SPECS);
        String specs2 = otherMap.get(SPECS);

        firstMap.putAll(otherMap);
        if (specs1 != null || specs2 != null) {
            val specsMap1 = Specs.parseSpecs(specs1);
            val specsMap2 = Specs.parseSpecs(specs2);
            val treeMap = Maps.newTreeMap();
            treeMap.putAll(specsMap1);
            treeMap.putAll(specsMap2);

            String specsJoin = mapJoiner.join(treeMap);
            firstMap.put(SPECS, specsJoin);
        }

        return firstMap;
    }

    public static boolean isFastWestCacheAnnotated(Class c) {
        for (Method m : c.getMethods()) {
            val yes = WestCacheOption.isFastWestCacheable(m);
            if (yes) return true;
        }

        return false;
    }
}
