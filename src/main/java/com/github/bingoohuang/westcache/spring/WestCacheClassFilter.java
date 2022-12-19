package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.spring.exclude.WestCacheExcludes;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.bingoohuang.westcache.utils.BlackListClass;
import com.github.bingoohuang.westcache.utils.Cglibs;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.aop.ClassFilter;

@Slf4j
public class WestCacheClassFilter implements ClassFilter {

    @Override
    public boolean matches(Class<?> targetClass) {
        if (targetClass.isInterface()) return false;

        val targetClassName = targetClass.getName();
        if (targetClassName.startsWith("com.sun.proxy.$Proxy")) return false;
        if (targetClassName.startsWith("java.")) return false;
        if (Cglibs.isProxyClass(targetClass)) return false;

        if (BlackListClass.inBlackList(targetClassName)) {
            log.debug("{} matched blacklist for westcache, ignored.", targetClass);
            return false;
        }

        for (val anno : WestCacheExcludes.excludeAnnoTypes()) {
            if (Anns.hasAnnotationInHierarchy(anno, targetClass)) return false;
        }

        val matched = Anns.isFastWestCacheAnnotated(targetClass);
        if (!matched) {
            log.debug("Try to parse {} for westcache and failed.", targetClass);
        }

        return matched;
    }
}
