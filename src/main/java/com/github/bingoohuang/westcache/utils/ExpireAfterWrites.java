package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.base.ExpireAfterWritable;
import com.github.bingoohuang.westcache.base.ExpireAfterWrite;
import com.github.bingoohuang.westcache.base.WestCacheException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
@Slf4j @UtilityClass
public class ExpireAfterWrites {
    public static String parseExpireAfterWrite(WestCacheOption option, Object obj) {
        if (option == null) return null;

        String expireWrite = option.getSpecs().get("expireAfterWrite");
        if (isNotBlank(expireWrite)) return expireWrite;

        if (obj == null) return null;
        if (obj instanceof ExpireAfterWritable) {
            return ((ExpireAfterWritable) obj).expireAfterWrite();
        }

        return getExpireAfterWrite(obj);
    }

    public static String getExpireAfterWrite(Object object) {
        val m = findExpireAfterWriteMethod(object);
        return m == null ? null : (String) Envs.invoke(m, object);
    }

    public static Method findExpireAfterWriteMethod(Object object) {
        val annClass = ExpireAfterWrite.class;
        for (Method m : object.getClass().getMethods()) {
            if (!m.isAnnotationPresent(annClass)) continue;

            if (isNormalStringMethodWithoutArgs(m)) return m;

            throw new WestCacheException("method "
                    + m.getDeclaringClass().getSimpleName()
                    + "." + m.getName() + " is invalid for @"
                    + annClass.getSimpleName());
        }

        return null;
    }

    private static boolean isNormalStringMethodWithoutArgs(Method method) {
        return !Modifier.isStatic(method.getModifiers())
                && Modifier.isPublic(method.getModifiers())
                && method.getParameterTypes().length == 0
                && method.getReturnType() == String.class;
    }
}
