package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import java.util.List;
import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
@Slf4j @UtilityClass
public class FastJsons {
    private static ParserConfig parseConfig = new ParserConfig();
    private static SerializeConfig serializeConfig = new SerializeConfig();

    static {
        parseConfig.putDeserializer(DateTime.class, new JsonJodaDeserializer());
        parseConfig.setAutoTypeSupport(true);
        serializeConfig.put(DateTime.class, new JsonJodaSerializer());
    }

    public static String json(Object obj, Method method) {
        val arg0GenericType = parseGenericArg0Type(method.getGenericReturnType());
        if (arg0GenericType instanceof Class) {
            return JSON.toJSONString(obj, serializeConfig);
        }

        if (arg0GenericType instanceof ParameterizedType) {
            val pt = (ParameterizedType) arg0GenericType;
            if (pt.getRawType() == List.class && pt.getActualTypeArguments()[0] instanceof Class) {
                return JSON.toJSONString(obj, serializeConfig);
            }
        }

        return JSON.toJSONString(obj, serializeConfig, SerializerFeature.WriteClassName);
    }

    public static String json(Object obj) {
        return JSON.toJSONString(obj, serializeConfig);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json) {
        return (T) JSON.parse(json, parseConfig);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Class<?> returnType) {
        return (T) JSON.parseObject(json, returnType, parseConfig);
    }

    @SneakyThrows @SuppressWarnings("unchecked")
    public static <T> T parse(String json, Method method, boolean silent) {
        val arg0GenericType = parseGenericArg0Type(method.getGenericReturnType());
        try {
            return (T) JSON.parseObject(json, arg0GenericType, parseConfig);
        } catch (Exception ex) {
            log.error("parse json for method cache error, method:{}, json:{}",
                    method, json, ex);

            if (!silent) throw ex;

            return arg0GenericType == String.class ? (T) json : null;
        }
    }

    public static Type parseGenericArg0Type(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) return genericType;

        val pt = (ParameterizedType) genericType;
        if (pt.getRawType() != Map.class) return genericType;

        val args = pt.getActualTypeArguments();
        val ownerType = pt.getOwnerType();
        return new ParameterizedTypeImpl(args, ownerType, LinkedHashMap.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(String json, TypeReference typeReference) {
        return (T) JSON.parseObject(json, typeReference.getType(), parseConfig);
    }
}
