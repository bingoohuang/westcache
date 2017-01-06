package com.github.bingoohuang.westcache.peng;

import com.github.bingoohuang.westcache.WestCacheable;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
@WestCacheable(flusher = "table", keyer = "simple")
public abstract class PengService {
    public abstract String getCitiesJSON(String provinceCode);

    public abstract List<String> getCitiesList(String provinceCode);

    public abstract List<CityBean> getCityBeans(String provinceCode);

    public abstract List<CityBean> getCities(String provinceCode);

    @WestCacheable(key = "mall.city")
    public abstract String getKeyCities(String provinceCode);

    // this methos is used to push the rotating checker into running
    public String firstPush() {
        return "first";
    }
}
