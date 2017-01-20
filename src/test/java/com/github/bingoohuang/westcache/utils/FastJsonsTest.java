package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/5.
 */
public class FastJsonsTest {
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class FastBean {
        private String name;
        private int age;
    }

    interface FastInterface {
        String string();

        List<String> strings();

        FastBean bean();

        List<FastBean> beans();
    }

    @Test @SneakyThrows
    public void string() {
        Method method = FastInterface.class.getMethod("string");
        String str = FastJsons.parse("\"abc\"", method);
        assertThat(str).isEqualTo("abc");
    }
    @Test @SneakyThrows
    public void stringPure() {
        Method method = FastInterface.class.getMethod("string");
        String str = FastJsons.parse("abc", method);
        assertThat(str).isEqualTo("abc");
    }


    @Test @SneakyThrows
    public void strings() {
        Method method = FastInterface.class.getMethod("strings");
        List<String> str = FastJsons.parse("[\"abc\",\"efg\"]", method);
        assertThat(str).isEqualTo(Lists.newArrayList("abc", "efg"));
    }

    @Test @SneakyThrows
    public void bean() {
        Method method = FastInterface.class.getMethod("bean");
        FastBean bean = FastJsons.parse("{\"name\":\"abc\", \"age\":123}", method);
        assertThat(bean).isEqualTo(new FastBean("abc", 123));
    }

    @Test @SneakyThrows
    public void beans() {
        Method method = FastInterface.class.getMethod("beans");
        List<FastBean> bean = FastJsons.parse("[{\"name\":\"abc\", \"age\":123}]", method);
        assertThat(bean).hasSize(1);
        assertThat(bean.get(0)).isEqualTo(new FastBean("abc", 123));
    }

    @Test @SneakyThrows
    public void badJson() {
        Method method = FastInterface.class.getMethod("string");
        String bean = FastJsons.parse("[{\"name\":\"abc\", \"age\":123]", method);
        assertThat(bean).isEqualTo("[{\"name\":\"abc\", \"age\":123]");
    }

    @Test(expected = JSONException.class) @SneakyThrows
    public void badJsonBean() {
        Method method = FastInterface.class.getMethod("bean");
        FastJsons.parse("{\"name\":\"abc\", \"age\":123", method);
    }
}
