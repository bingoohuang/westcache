package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.WestCacheable;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/5.
 */
public class AnnsTest {
    @WestCacheable(config = "default", manager = "default",
            specs = "s2=v2,s1=v0") public interface Interface1 {
        @WestCacheable(manager = "guava")
        String m1();

        @WestCacheable(specs = "s1=v1")
        String m2();
    }

    @Test @SneakyThrows
    public void testM1() {
        Method m1 = Interface1.class.getMethod("m1");
        Map<String, String> attrs = Anns.parseWestCacheable(m1, WestCacheable.class).getAnnotationAttributes();
        assertThat(attrs).isEqualTo(ImmutableMap.of(
                "config", "default",
                "manager", "guava",
                "specs", "s1=v0,s2=v2"));
    }

    @Test @SneakyThrows
    public void testM2() {
        Method m2 = Interface1.class.getMethod("m2");
        Map<String, String> attrs2 = Anns.parseWestCacheable(m2, WestCacheable.class).getAnnotationAttributes();
        assertThat(attrs2).isEqualTo(ImmutableMap.of(
                "config", "default",
                "manager", "default",
                "specs", "s1=v1,s2=v2"));
    }

    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(snapshot = "file", key = "abc")
    public @interface AnnCom {
        String sth() default "";

        String key() default "";
    }

    interface Interface2 {
        @AnnCom(sth = "xx")
        String m1();

        @AnnCom(key = "yy")
        String m2();
    }

    @Test @SneakyThrows
    public void testAnnComM1() {
        Method m = Interface2.class.getMethod("m1");
        Map<String, String> attrs = Anns.parseWestCacheable(m, WestCacheable.class).getAnnotationAttributes();

        assertThat(attrs).isEqualTo(ImmutableMap.of(
                "snapshot", "file",
                "key", "abc",
                "sth", "xx"));

        WestCacheOption option = WestCacheOption.parseWestCacheable(m);
        assertThat(option.getSpecs()).isEqualTo(ImmutableMap.of("sth", "xx"));
    }

    @Test @SneakyThrows
    public void testAnnComM2() {
        Method m = Interface2.class.getMethod("m2");
        Map<String, String> attrs = Anns.parseWestCacheable(m, WestCacheable.class).getAnnotationAttributes();

        assertThat(attrs).isEqualTo(ImmutableMap.of(
                "snapshot", "file",
                "key", "yy"));

        WestCacheOption option = WestCacheOption.parseWestCacheable(m);
        assertThat(option.getSpecs()).isEmpty();
    }

    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(snapshot = "file", key = "abc")
    public @interface AnnInerit {
        String key() default "";
    }

    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @AnnInerit
    public @interface AnnSon {
        String key() default "";
    }

    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @AnnSon
    public @interface AnnSon0 {
        String key() default "";
    }


    public interface InterfaceSon {
        @AnnSon0(key = "guava")
        String m1();
    }

    @Test @SneakyThrows
    public void testInterfaceSon() {
        Method m = InterfaceSon.class.getMethod("m1");
        Map<String, String> attrs = Anns.parseWestCacheable(m, WestCacheable.class).getAnnotationAttributes();
        assertThat(attrs).isEqualTo(ImmutableMap.of(
                "snapshot", "file",
                "key", "guava"));

    }
}
