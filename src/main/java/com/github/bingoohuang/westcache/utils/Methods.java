package com.github.bingoohuang.westcache.utils;

import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
        try {
            val interfaceMethod = aClass.getMethod(method.getName(), method.getParameterTypes());
            if (Modifier.isStatic(interfaceMethod.getModifiers())) return;
            if (Modifier.isPrivate(interfaceMethod.getModifiers())) return;

            allMethods.add(interfaceMethod);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        }
    }

}
