package com.github.bingoohuang.westcache.spring;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WestCacheableAdvisor extends AbstractPointcutAdvisor {
    final transient StaticMethodMatcherPointcut pointcut = new WestCacheStaticMethodMatcherPointcut();
    @Autowired transient WestCacheableInterceptor interceptor;

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.interceptor;
    }
}

