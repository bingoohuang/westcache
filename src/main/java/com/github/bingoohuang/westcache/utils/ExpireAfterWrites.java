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
    public static String parseExpireAfterWrite(WestCacheOption opt, Object o) {
        if (opt == null) return null;

        val expireWrite = opt.getSpecs().get("expireAfterWrite");
        if (isNotBlank(expireWrite)) return expireWrite;

        if (o == null) return null;
        if (o instanceof ExpireAfterWritable) {
            return ((ExpireAfterWritable) o).expireAfterWrite();
        }

        return getExpireAfterWrite(o);
    }

    public static String getExpireAfterWrite(Object o) {
        val m = findExpireAfterWriteMethod(o);
        return m == null ? null : (String) Envs.invoke(m, o);
    }

    public static Method findExpireAfterWriteMethod(Object o) {
        val annClass = ExpireAfterWrite.class;
        for (val m : o.getClass().getMethods()) {
            if (!m.isAnnotationPresent(annClass)) continue;
            if (isNormalStringMethodWithoutArgs(m)) return m;

            throw new WestCacheException("method "
                    + m.getDeclaringClass().getSimpleName()
                    + "." + m.getName() + " is invalid for @"
                    + annClass.getSimpleName());
        }

        return null;
    }

    private static boolean isNormalStringMethodWithoutArgs(Method m) {
        return !Modifier.isStatic(m.getModifiers())
                && Modifier.isPublic(m.getModifiers())
                && m.getParameterTypes().length == 0
                && m.getReturnType() == String.class;
    }
}
