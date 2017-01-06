package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
@UtilityClass
public class FastJsons {
    public String json(Object obj) {
        return JSON.toJSONString(obj);
    }

    public <T> T parse(String json) {
        return (T) JSON.parse(json);
    }

    public Object parse(String json, Class<?> returnType) {
        return JSON.parseObject(json, returnType);
    }

    @SneakyThrows
    public <T> T parse(String json, Method method) {
        Class<?> returnType = method.getReturnType();

        Type genericReturnType = method.getGenericReturnType();

        boolean isCollectionGeneric =
                genericReturnType instanceof ParameterizedType
                        && Collection.class.isAssignableFrom(returnType);
        if (isCollectionGeneric) {
            val pType = (ParameterizedType) genericReturnType;
            val itemClass = (Class) pType.getActualTypeArguments()[0];
            return (T) JSON.parseArray(json, itemClass);
        }

        try {
            return (T) JSON.parseObject(json, returnType);
        } catch (Exception ex) {
            if (returnType == String.class) return (T) json;
            throw ex;
        }
    }

    public static <T> T parse(String json, TypeReference typeReference) {
        return (T) JSON.parseObject(json, typeReference);
    }
}
