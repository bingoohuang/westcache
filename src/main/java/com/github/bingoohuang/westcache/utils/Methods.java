package com.github.bingoohuang.westcache.utils;

import lombok.val;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public class Methods {
    /*
     * Gets an array of all methods in a class hierarchy walking up to parent classes
     */
    public static Set<Method> getAllMethodsInHierarchy(Method method) {
        LinkedHashSet<Method> allMethods = new LinkedHashSet<>();
        val declaringClass = method.getDeclaringClass();
        return getAllMethodsInHierarchy(allMethods, declaringClass, method);
    }

    public static Set<Method> getAllMethodsInHierarchy(Set<Method> allMethods, Class<?> objectClass, Method method) {
        allMethods.add(method);

        Class<?> superclass = objectClass.getSuperclass();
        if (superclass != null) {
            addMethod(allMethods, superclass, method);
            getAllMethodsInHierarchy(allMethods, superclass, method);
        }

        for (val interfaceClass : objectClass.getInterfaces()) {
            addMethod(allMethods, interfaceClass, method);
        }

        return allMethods;
    }

    private static void addMethod(Set<Method> allMethods, Class<?> aClass, Method method) {
        for (val declaredMethod : aClass.getDeclaredMethods()) {
            if (!declaredMethod.getName().equals(method.getName())) continue;
            if (sameParameterTypes(declaredMethod.getParameterTypes(),
                    method.getParameterTypes())) {
                allMethods.add(declaredMethod);
            }
        }

    }

    private static boolean sameParameterTypes(Class<?>[] pTypes1, Class<?>[] pTypes2) {
        if (pTypes1.length != pTypes2.length) return false;

        for (int i = 0; i < pTypes1.length; ++i) {
            if (pTypes1[i] != pTypes2[i]) return false;
        }

        return true;
    }

}
