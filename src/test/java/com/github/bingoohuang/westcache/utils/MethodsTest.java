package com.github.bingoohuang.westcache.utils;

import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

public class MethodsTest {
    public interface Interface1 {
        void method1();
    }

    public interface Interface2 {
        void method1();
    }

    public interface Interface3 {
        void method2();
    }

    public static class ParentClass {
        public void method1() {

        }
    }

    public abstract static class Class1 extends ParentClass implements Interface1, Interface2, Interface3 {
        @Override public void method1() {

        }
    }

    @Test @SneakyThrows
    public void test1() {
        Method method1 = Class1.class.getMethod("method1");
        Set<Method> methods = Methods.getAllMethodsInHierarchy(method1);
        Method interface1Method1 = Interface1.class.getMethod("method1");
        Method interface2Method1 = Interface2.class.getMethod("method1");
        Method parentMethod1 = ParentClass.class.getMethod("method1");
        assertThat(methods).containsExactly(method1, interface1Method1, interface2Method1, parentMethod1);
    }
}
