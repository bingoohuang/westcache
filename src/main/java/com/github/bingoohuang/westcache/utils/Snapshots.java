package com.github.bingoohuang.westcache.utils;

import java.io.File;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/7.
 */
public class Snapshots {
    public static String USER_HOME = System.getProperty("user.home");
    public static String EXTENSION = ".westcache";
    public static File CACHE_HOME = new File(USER_HOME, EXTENSION);

    public static File getSnapshotFile(String cacheKey) {
        File westCacheHome = tryCreateWestCacheHome();
        return new File(westCacheHome, cacheKey + EXTENSION);
    }

    private static File tryCreateWestCacheHome() {
        CACHE_HOME.mkdirs();
        return CACHE_HOME;
    }
}
