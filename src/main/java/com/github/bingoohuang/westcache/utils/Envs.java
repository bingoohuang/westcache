package com.github.bingoohuang.westcache.utils;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class Envs {
    public static boolean hasSpring = classExists("org.springframework.context.ApplicationContext");
    public static boolean hasDiamond = classExists("org.n3r.diamond.client.DiamondListener");
    public static boolean hasExpiring = classExists("net.jodah.expiringmap.ExpiringMap");
    public static boolean hasJedis = classExists("redis.clients.jedis.JedisCommands");
    public static boolean hasEql = classExists("org.n3r.eql.eqler.EqlerFactory");

    public static boolean classExists(String className) {
        try {
            Class.forName(className, false, Envs.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
