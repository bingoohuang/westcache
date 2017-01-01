package com.github.bingoohuang.westcache.outofbox;

import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
@EqlerConfig
public interface TableCacheFlusherDao {
    @Sql("DROP TABLE IF EXISTS WESTCACHE_FLUSHER;" +
            "CREATE TABLE WESTCACHE_FLUSHER (" +
            "  CACHE_KEY     VARCHAR(1000)              NOT NULL PRIMARY KEY," +
            "  KEY_MATCH     VARCHAR(20) DEFAULT 'full' NOT NULL COMMENT 'full:full match,prefix:prefix match'," +
            "  VALUE_VERSION TINYINT DEFAULT 0          NOT NULL COMMENT 'version of cache, increment it to update cache'," +
            "  CACHE_STATE   TINYINT DEFAULT 1          NOT NULL COMMENT 'direct json value for the cache'," +
            "  VALUE_TYPE    VARCHAR(20) DEFAULT 'none' NOT NULL COMMENT 'value access type, direct: use direct json in DIRECT_VALUE field'," +
            "  SPECS         VARCHAR(1000)              NULL     COMMENT 'specs for extension'," +
            "  DIRECT_VALUE  TEXT)ENGINE=InnoDB DEFAULT CHARSET=utf8")
    void setup();

    @Sql("SELECT CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE, SPECS " +
            "FROM WESTCACHE_FLUSHER WHERE CACHE_STATE = 1")
    List<WestCacheFlusherBean> selectAllBeans();

    @Sql("SELECT DIRECT_VALUE FROM WESTCACHE_FLUSHER " +
            "WHERE CACHE_KEY = ## AND CACHE_STATE = 1")
    String getDirectValue(String key);

    @Sql("INSERT INTO WESTCACHE_FLUSHER(CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE, SPECS) " +
            "VALUES(#?#, #?#, #?#, #?#, #?#)")
    void addBean(WestCacheFlusherBean bean);

    @Sql("UPDATE WESTCACHE_FLUSHER " +
            "SET KEY_MATCH = #?#, VALUE_VERSION = #?#, VALUE_TYPE = #?#, SPECS = #?# " +
            "WHERE CACHE_KEY = #?#")
    void updateBean(WestCacheFlusherBean bean);

    @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1," +
            "DIRECT_VALUE = #2# " +
            "WHERE CACHE_KEY = #1#")
    int updateDirectValue(String cacheKey, String directValue);

    @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1 " +
            "WHERE CACHE_KEY = ##")
    void upgradeVersion(String key);
}
