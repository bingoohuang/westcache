package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.experimental.UtilityClass;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
@UtilityClass
public class FastJsons {
    public String json(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteClassName);
    }

    public Object parse(String json) {
        return JSON.parse(json);
    }

    public Object parse(String json, Class<?> returnType) {
        Object object = JSON.parse(json);
        if (returnType.isInstance(object)) return object;

        return JSON.parseObject(json, returnType);
    }
}
