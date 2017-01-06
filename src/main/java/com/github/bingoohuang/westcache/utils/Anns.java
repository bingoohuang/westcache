package com.github.bingoohuang.westcache.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class Anns {
    public static Map<String, String> parseWestCacheable(
            Method method,
            Class<? extends Annotation> annClass) {
        val methodAnn = method.getAnnotation(annClass);
        val declaringClass = method.getDeclaringClass();
        val classAnn = declaringClass.getAnnotation(annClass);

        if (methodAnn != null || classAnn != null) {
            Map<String, String> methodAttrs = getAllAttrs(methodAnn);
            Map<String, String> classAttrs = getAllAttrs(classAnn);
            return mergeMap(classAttrs, methodAttrs);
        }

        Set<Annotation> setAnns = Sets.newHashSet();
        val annM = searchAnn(setAnns, method.getAnnotations(), annClass);
        val annC = searchAnn(setAnns, declaringClass.getAnnotations(), annClass);
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

    public static Map<String, String> searchAnn(
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

        Method[] methods = ann.annotationType().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length > 0) continue;
            if (method.getReturnType() != String.class) continue;

            try {
                String value = (String) method.invoke(ann);
                if (isNotEmpty(value)) attrs.put(method.getName(), value);
            } catch (Exception ex) {
                // ignore
            }
        }
        return attrs;
    }

    public static boolean anyOf(String str, String... ranges) {
        for (String val : ranges) {
            if (StringUtils.equals(val, str)) return true;
        }

        return false;
    }

    private static Map<String, String> parseRecursiveAnn(
            Set<Annotation> setAnns,
            Annotation ann,
            Class<?> annClz) {
        if (ann.toString().startsWith("@java.lang.")) return null;
        if (setAnns.contains(ann)) return null;

        setAnns.add(ann);

        val annotations = ann.annotationType().getAnnotations();
        for (val typeAnn : annotations) {
            if (annClz.isInstance(typeAnn)) {
                val annAttrs = getAllAttrs(typeAnn);
                val thisAttrs = getAllAttrs(ann);
                merge(annAttrs, thisAttrs);
                return annAttrs;
            }
        }

        for (val annotation : annotations) {
            val attrs = parseRecursiveAnn(setAnns, annotation, annClz);
            if (attrs != null) {
                val thisAttrs = getAllAttrs(ann);
                merge(attrs, thisAttrs);
                return attrs;
            }
        }

        return null;
    }

    static Joiner.MapJoiner mapJoiner = Joiner.on(';').withKeyValueSeparator('=');

    private static void merge(Map<String, String> firstMap,
                              Map<String, String> otherMap) {
        String specs1 = firstMap.get("specs");
        String specs2 = otherMap.get("specs");

        firstMap.putAll(otherMap);
        if (specs1 != null || specs2 != null) {
            Map<String, String> specsMap1 = Specs.parseSpecs(specs1);
            Map<String, String> specsMap2 = Specs.parseSpecs(specs2);
            Map<String, String> treeMap = Maps.newTreeMap();
            treeMap.putAll(specsMap1);
            treeMap.putAll(specsMap2);

            String specsJoin = mapJoiner.join(treeMap);
            firstMap.put("specs", specsJoin);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Annotation ann, String attrName) {
        if (ann == null || isBlank(attrName)) return null;

        try {
            val annType = ann.annotationType();
            Method method = annType.getDeclaredMethod(attrName);
            makeAccessible(method);
            return (T) method.invoke(ann);
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Annotation ann, String attrName) {
        if (ann == null || isBlank(attrName)) return null;
        val annotationType = ann.annotationType();

        return getDefaultValue(annotationType, attrName);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getDefaultValue(Class<?> annType, String attrName) {
        try {
            Method method = annType.getDeclaredMethod(attrName);
            return (T) method.getDefaultValue();
        } catch (Exception ex) {
            return null;
        }
    }


    public static void makeAccessible(Method method) {
        if (method.isAccessible()) return;

        method.setAccessible(true);
    }
}
