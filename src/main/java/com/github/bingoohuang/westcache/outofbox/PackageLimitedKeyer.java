package com.github.bingoohuang.westcache.outofbox;

import com.github.bingoohuang.westcache.base.WestCacheException;
import com.github.bingoohuang.westcache.keyer.SimpleKeyer;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Splitter;
import lombok.val;
import org.n3r.diamond.client.Miner;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
public class PackageLimitedKeyer extends SimpleKeyer {
    public static final String GROUP = "west.cache.packagelimit";
    public static final String DATAID = "packages";

    private static Splitter splitter = Splitter.onPattern("[\r\n;\\s]")
            .omitEmptyStrings().trimResults();

    @Override
    public String getCacheKey(WestCacheOption option,
                              String methodName,
                              Object bean,
                              Object... args) {
        String packageName = bean.getClass().getPackage().getName();
        val pkgsConfig = new Miner().getStone(GROUP, DATAID);
        if (pkgsConfig == null) reportInvalidPackage(packageName);

        val pkgs = splitter.splitToList(pkgsConfig);
        if (!pkgs.contains(packageName)) reportInvalidPackage(packageName);

        return super.getCacheKey(option, methodName, bean, args);
    }

    private void reportInvalidPackage(String packageName) {
        throw new WestCacheException(packageName
                + " is not allowed for the cache key");
    }
}
