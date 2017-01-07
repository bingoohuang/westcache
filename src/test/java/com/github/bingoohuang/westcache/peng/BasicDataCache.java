package com.github.bingoohuang.westcache.peng;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BasicDataCache {
    @MallRedisCache(key = "mall.commonParam", expireAfterWrite = "10m")
    public List<CommparaBean> qryCommParam(String paramAttr) {
        return Lists.newArrayList(
                new CommparaBean(paramAttr, "code", "name")
        );
    }

    @MallRedisCache
    public String firstPush() {
        return "first";
    }
}

