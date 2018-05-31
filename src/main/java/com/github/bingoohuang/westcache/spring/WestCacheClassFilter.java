package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.cglib.Cglibs;
import com.github.bingoohuang.westcache.utils.Anns;
import com.github.bingoohuang.westcache.utils.BlackListClass;
import com.github.bingoohuang.westcache.utils.Envs;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.springframework.aop.ClassFilter;

@Slf4j
public class WestCacheClassFilter implements ClassFilter {
    public static final boolean hasEqler;


    static {
        hasEqler = Envs.classExists("org.n3r.eql.eqler.annotations.Eqler");
    }

    @Override public boolean matches(Class<?> targetClass) {
        if (targetClass.isInterface()) return false;

        val targetClassName = targetClass.getName();
        if (targetClassName.startsWith("com.sun.proxy.$Proxy")) return false;
        if (targetClassName.startsWith("java.")) return false;
        if (Cglibs.isProxyClass(targetClass)) return false;

        if (BlackListClass.inBlackList(targetClassName)) {
            log.debug("{} matched blacklist for westcache, ignored.", targetClass);
            return false;
        }

        if (hasEqler) {
            if (Anns.hasAnnotationInHierarchy(Eqler.class, targetClass)) return false;
            if (Anns.hasAnnotationInHierarchy(EqlerConfig.class, targetClass)) return false;
        }

        val matched = Anns.isFastWestCacheAnnotated(targetClass);
        if (!matched) {
            log.debug("Try to parse {} for westcache and failed.", targetClass);
        }

        return matched;
    }
}
