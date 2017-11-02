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
        val allMethods = new LinkedHashSet<Method>();
        return getAllMethodsInHierarchy(allMethods, method.getDeclaringClass(), method);
    }

    public static Set<Method> getAllMethodsInHierarchy(Set<Method> allMethods, Class<?> objectClass, Method method) {
        allMethods.add(method);

        if (objectClass.getSuperclass() != null) {
            addMethod(allMethods, objectClass.getSuperclass(), method);
            getAllMethodsInHierarchy(allMethods, objectClass.getSuperclass(), method);
        }

        for (val interfaceClass : objectClass.getInterfaces()) {
            addMethod(allMethods, interfaceClass, method);
        }

        return allMethods;
    }

    private static void addMethod(Set<Method> allMethods, Class<?> aClass, Method method) {
        for (val declaredMethod : aClass.getDeclaredMethods()) {
            if (!declaredMethod.getName().equals(method.getName())) continue;
            if (sameParameterTypes(declaredMethod.getParameterTypes(), method.getParameterTypes())) {
                allMethods.add(declaredMethod);
            }
        }

    }

    private static boolean sameParameterTypes(Class<?>[] parameterTypes1, Class<?>[] parameterTypes2) {
        if (parameterTypes1.length != parameterTypes2.length) return false;

        for (int i = 0; i < parameterTypes1.length; ++i) {
            if (parameterTypes1[i] != parameterTypes2[i]) return false;
        }

        return true;
    }

}
