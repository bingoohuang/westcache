package com.github.bingoohuang.westcache.dao;

import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@EqlerConfig("mysql")
public interface WestCacheFlusherDao {
    void setup();

    @Sql("SELECT CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE, SPECS " +
            "FROM WESTCACHE_FLUSHER WHERE CACHE_STATE = 1")
    List<WestCacheFlusherBean> queryAllBeans();

    @Sql("SELECT DIRECT_VALUE FROM WESTCACHE_FLUSHER " +
            "WHERE CACHE_KEY = ## AND CACHE_STATE = 1")
    String getDirectValue(String key);

    @Sql("INSERT INTO WESTCACHE_FLUSHER(CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE, SPECS) " +
            "VALUES(#?#, #?#, #?#, #?#, #?#)")
    void addWestCacheFlusherBean(WestCacheFlusherBean bean);

    @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1," +
            "DIRECT_VALUE = #2# " +
            "WHERE CACHE_KEY = #1#")
    int updateDirectValue(String cacheKye, String directValue);

    @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1 " +
            "WHERE CACHE_KEY = ##")
    void updateWestCacheFlusherBean(String key);
}
