package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class JsonJodaDeserializer implements ObjectDeserializer {
    @SuppressWarnings("unchecked") @Override
    public DateTime deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Long longValue = parser.parseObject(Long.class);
        return new DateTime(longValue);
    }

    @Override public int getFastMatchToken() {
        return JSONToken.LITERAL_INT;
    }
}
