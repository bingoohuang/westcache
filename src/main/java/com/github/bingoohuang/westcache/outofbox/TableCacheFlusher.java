package com.github.bingoohuang.westcache.outofbox;

import com.github.bingoohuang.westcache.flusher.TableBasedCacheFlusher;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.Specs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.eqler.EqlerFactory;

import java.util.List;
import java.util.concurrent.Callable;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
                                     WestCacheFlusherBean bean) {
        lastReadDirectValue = System.currentTimeMillis();

        val specs = Specs.parseSpecs(bean.getSpecs());
        val readBy = specs.get("readBy");
        if ("redis".equals(readBy)) {
            String key = Redis.PREFIX + bean.getCacheKey();
            val value = Redis.getRedis(option).get(key);
            if (isNotBlank(value)) {
                return FastJsons.parse(value, option.getMethod());
            }
        } else if ("loader".equals(readBy)) {
            val loaderClass = specs.get("loaderClass");
            if (isNotBlank(loaderClass)) {
                Class<?> clazz = Class.forName(loaderClass);
                val loader = (Callable) clazz.newInstance();
                return loader.call();
            }
        }

        String directJson = dao.getDirectValue(bean.getCacheKey());
        if (isBlank(directJson)) return null;
        return FastJsons.parse(directJson);
    }
}
