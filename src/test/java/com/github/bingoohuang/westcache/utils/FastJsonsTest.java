package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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

        Map<String, FastBean> maps();
    }


    @SneakyThrows @Test
    public void testGeneric() {
        Map<String, FastBean> map = Maps.newHashMap();
        FastBean a = new FastBean("bingoo", 11);
        map.put("a", a);
        FastBean b = new FastBean("huang", 11);
        map.put("b", b);
        String json = FastJsons.json(map);
        assertThat(json).isEqualTo("{\"a\":{\"age\":11,\"name\":\"bingoo\"},\"b\":{\"age\":11,\"name\":\"huang\"}}");

        Method mapsMethod = FastInterface.class.getMethod("maps");
        Method beansMethod = FastInterface.class.getMethod("beans");

        Map<String, FastBean> o = JSON.parseObject(json, mapsMethod.getGenericReturnType());
        assertThat(o).isEqualTo(map);


        List<FastBean> beans = Lists.newArrayList(a, b);
        String json1 = FastJsons.json(beans);

        List<FastBean> o1 = JSON.parseObject(json1, beansMethod.getGenericReturnType());
        assertThat(o1).isEqualTo(beans);
    }

    @Test @SneakyThrows
    public void string() {
        Method method = FastInterface.class.getMethod("string");
        String str = FastJsons.parse("\"abc\"", method, true);
        assertThat(str).isEqualTo("abc");
    }

    @Test @SneakyThrows
    public void stringPure() {
        Method method = FastInterface.class.getMethod("string");
        String str = FastJsons.parse("abc", method, true);
        assertThat(str).isEqualTo("abc");
    }


    @Test @SneakyThrows
    public void strings() {
        Method method = FastInterface.class.getMethod("strings");
        List<String> str = FastJsons.parse("[\"abc\",\"efg\"]", method, true);
        assertThat(str).isEqualTo(Lists.newArrayList("abc", "efg"));
    }

    @Test @SneakyThrows
    public void bean() {
        Method method = FastInterface.class.getMethod("bean");
        FastBean bean = FastJsons.parse("{\"name\":\"abc\", \"age\":123}", method, true);
        assertThat(bean).isEqualTo(new FastBean("abc", 123));
    }

    @Test @SneakyThrows
    public void beans() {
        Method method = FastInterface.class.getMethod("beans");
        List<FastBean> bean = FastJsons.parse("[{\"name\":\"abc\", \"age\":123}]", method, true);
        assertThat(bean).hasSize(1);
        assertThat(bean.get(0)).isEqualTo(new FastBean("abc", 123));
    }

    @Test @SneakyThrows
    public void badJson() {
        Method method = FastInterface.class.getMethod("string");
        String bean = FastJsons.parse("[{\"name\":\"abc\", \"age\":123]", method, true);
        assertThat(bean).isEqualTo("[{\"name\":\"abc\", \"age\":123]");
    }

    @Test(expected = JSONException.class) @SneakyThrows
    public void badJsonBean() {
        Method method = FastInterface.class.getMethod("bean");
        FastJsons.parse("{\"name\":\"abc\", \"age\":123", method, false);
    }
}
