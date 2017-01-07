package com.github.bingoohuang.westcache.outofbox;

import com.alibaba.fastjson.TypeReference;
import com.github.bingoohuang.westcache.flusher.DirectValueType;
import com.github.bingoohuang.westcache.flusher.TableBasedCacheFlusher;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.Specs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.n3r.eql.eqler.EqlerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
public class TableCacheFlusher extends TableBasedCacheFlusher {
    @Getter volatile long lastReadDirectValue;
    @Getter TableCacheFlusherDao dao
            = EqlerFactory.getEqler(TableCacheFlusherDao.class);

    @Override protected List<WestCacheFlusherBean> queryAllBeans() {
        return dao.selectAllBeans();
    }

    @Override @SneakyThrows
    protected Object readDirectValue(WestCacheOption option,
                                     WestCacheFlusherBean bean,
                                     DirectValueType type) {
        lastReadDirectValue = System.currentTimeMillis();

        val specs = Specs.parseSpecs(bean.getSpecs());
        val readBy = specs.get("readBy");
        if ("redis".equals(readBy)) {
            val key = Redis.PREFIX + bean.getCacheKey();
            val value = Redis.getRedis(option).get(key);
            if (StringUtils.isNotBlank(value)) {
                return FastJsons.parse(value, option.getMethod());
            }
        } else if ("loader".equals(readBy)) {
            val loaderClass = specs.get("loaderClass");
            if (StringUtils.isNotBlank(loaderClass)) {
                val clazz = Class.forName(loaderClass);
                val loader = (Callable) clazz.newInstance();
                return loader.call();
            }
        }

        val directJson = dao.getDirectValue(bean.getCacheKey());
        if (StringUtils.isBlank(directJson)) return null;

        switch (type) {
            case FULL:
                return FastJsons.parse(directJson, option.getMethod());
            case SUB:
                return FastJsons.parse(directJson,
                        new TypeReference<Map<String, String>>() {
                        });
        }

        return null;
    }
}
