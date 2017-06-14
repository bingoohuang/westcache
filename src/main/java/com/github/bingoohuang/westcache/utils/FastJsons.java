package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Method;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
@Slf4j @UtilityClass
public class FastJsons {
    public static String json(Object obj) {
        return JSON.toJSONString(obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json) {
        return (T) JSON.parse(json);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Class<?> returnType) {
        return (T) JSON.parseObject(json, returnType);
    }

    @SneakyThrows @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Method method, boolean silent) {
        val genericType = method.getGenericReturnType();

        try {
            return (T) JSON.parseObject(json, genericType);
        } catch (Exception ex) {
            if (silent) {
                log.warn("parse json json {} for method {} error", json, method, ex);
                return genericType == String.class ? (T) json : null;
            }
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, TypeReference typeReference) {
        return (T) JSON.parseObject(json, typeReference);
    }


}
