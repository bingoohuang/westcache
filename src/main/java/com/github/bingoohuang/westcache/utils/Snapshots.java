package com.github.bingoohuang.westcache.utils;

import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/7.
 */
@UtilityClass
public class Snapshots {
    static final String USER_HOME = System.getProperty("user.home");
    public static final String EXTENSION = ".westcache";
    public static final File CACHE_HOME = new File(USER_HOME, EXTENSION);

    public static File getSnapshotFile(String cacheKey) {
        CACHE_HOME.mkdirs();
        return new File(CACHE_HOME, cacheKey + EXTENSION);
    }

}
