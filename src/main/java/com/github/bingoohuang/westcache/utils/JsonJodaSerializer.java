package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import lombok.val;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Type;

public class JsonJodaSerializer implements ObjectSerializer {
    @Override
    public void write(JSONSerializer serializer, Object object,
                      Object fieldName, Type fieldType, int features
    ) throws IOException {
        val out = serializer.out;

        val value = (DateTime) object;
        if (value == null) {
            out.writeNull();
            return;
        }

        out.writeLong(value.getMillis());
    }
}
