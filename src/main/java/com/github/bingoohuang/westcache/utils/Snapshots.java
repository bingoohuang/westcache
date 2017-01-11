package com.github.bingoohuang.westcache.utils;

import java.io.File;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/7.
 */
public abstract class Snapshots {
    final static String USER_HOME = System.getProperty("user.home");
    public final static String EXTENSION = ".westcache";
    public final static File CACHE_HOME = new File(USER_HOME, EXTENSION);

    public static File getSnapshotFile(String cacheKey) {
        CACHE_HOME.mkdirs();
        return new File(CACHE_HOME, cacheKey + EXTENSION);
    }

}
