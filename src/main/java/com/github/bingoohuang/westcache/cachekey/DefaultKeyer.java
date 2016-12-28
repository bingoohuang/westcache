package com.github.bingoohuang.westcache.cachekey;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.github.bingoohuang.westcache.base.WestCacheKeyer;
import com.github.bingoohuang.westcache.cglib.Cglibs;
import com.google.common.base.Splitter;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class DefaultKeyer extends WestCacheKeyer {
    @Override
    public String getCacheKey(WestCacheOption option,
                              String methodName,
                              Object bean,
                              Object... args) {
        if (option.getKey().length() > 0) return option.getKey();

        val superClassName = Cglibs.getSuperClassName(bean);
        val dottedInnerClassName = superClassName.replace('$', '.');
        val baseCacheKey = dottedInnerClassName + "." + methodName;

        val staticKey = parseStaticKey(option);
        val mainPart = option.getSnapshot() != null
                || "yes".equalsIgnoreCase(staticKey)
                ? baseCacheKey
                : baseCacheKey + "." + bean.hashCode();

        if (args.length == 0) return mainPart;

        return mainPart + joinArgs(args);
    }

    private String parseStaticKey(WestCacheOption option) {
        val mapSplitter = Splitter.on(';').withKeyValueSeparator('=');
        if (option.getSpecs().isEmpty()) return null;

        val map = mapSplitter.split(option.getSpecs());
        return map.get("static.key");
    }

    public static String joinArgs(Object[] args) {
        val argsPart = new StringBuilder();

        for (val arg : args) {
            argsPart.append("_");
            if (arg == null) argsPart.append("null");
            else argsPart.append(arg);
        }

        return argsPart.toString();
    }
}
