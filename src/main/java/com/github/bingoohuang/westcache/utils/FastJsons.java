package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joda.time.DateTime;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
@Slf4j @UtilityClass
public class FastJsons {
    static {
        SerializeConfig.getGlobalInstance().put(DateTime.class,
                new JsonJodaSerializer());
        ParserConfig.getGlobalInstance().putDeserializer(DateTime.class,
                new JsonJodaDeserializer());
    }

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
        val genericType = parseMethodGenericReturnType(method);
        try {
            return (T) JSON.parseObject(json, genericType);
        } catch (Exception ex) {
            log.error("parse json for method cache error, method:{}, json:{}",
                    method, json, ex);

            if (!silent) throw ex;

            return genericType == String.class ? (T) json : null;
        }
    }

    private static Type parseMethodGenericReturnType(Method method) {
        val genericType = method.getGenericReturnType();
        if (!(genericType instanceof ParameterizedType)) return genericType;

        val pt = (ParameterizedType) genericType;
        if (pt.getRawType() != Map.class) return genericType;

        val args = pt.getActualTypeArguments();
        val ownerType = pt.getOwnerType();
        return new ParameterizedTypeImpl(args, ownerType, LinkedHashMap.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, TypeReference typeReference) {
        return (T) JSON.parseObject(json, typeReference);
    }
}
