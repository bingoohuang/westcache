package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.deserializer.ExtraProcessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/5.
 */
public class FastJsonsTest {
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class JodaBean {
        private DateTime updateTime;
    }

    @Test
    public void testJodaTime() {
        val jodaBean = new JodaBean();
        val timeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        val updateTime = timeFormatter.parseDateTime("2017-07-27 15:46:00.000");
        jodaBean.setUpdateTime(updateTime);

        val json = FastJsons.json(jodaBean);
        assertThat(json).isEqualTo("{\"updateTime\":\"2017-07-27 15:46:00.000\"}");

        val jodaBean2 = FastJsons.parse(json, JodaBean.class);
        assertThat(jodaBean).isEqualTo(jodaBean2);

        val json3 = "{\"updateTime\":" +  updateTime.getMillis() + "}";
        val jodaBean3 = FastJsons.parse(json3, JodaBean.class);
        assertThat(jodaBean).isEqualTo(jodaBean3);
    }

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

        Map<String, String> stringMaps();
    }


    @SneakyThrows @Test
    public void testMapSeq() {
        Method mapsMethod = FastInterface.class.getMethod("stringMaps");

        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("bbb", "222");
        map.put("aaa", "111");

        String json = FastJsons.json(map);
        Map<String, String> map2 = FastJsons.parse(json, mapsMethod, true);
        Collection<String> values = map2.values();
        assertThat(values).containsExactly("222", "111").inOrder();
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


    public static ExtraProcessor extraKeyProcessor = new ExtraProcessor() {
        @Override public void processExtra(Object object, String key, Object value) {
            throw new UnsupportedKeyException(key);
        }
    };

    // 使用ExtraProcessor 处理多余字段
    // https://github.com/alibaba/fastjson/wiki/ParseProcess
    // https://github.com/alibaba/fastjson/wiki/ExtraProcessable
    public static <T> T parseObject(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz, extraKeyProcessor);
    }

    @Data
    public static class BeanBean {
        private String a;
    }

    @Test(expected = JSONException.class)
    public void extraKeyTest() {
        String json = "{\"a\":\"123\",\"b\":\"456\"}";
        parseObject(json, BeanBean.class);
    }

    private static class UnsupportedKeyException extends RuntimeException {
        public UnsupportedKeyException(String key) {
            super("Unsupported key " + key);
        }
    }
}
