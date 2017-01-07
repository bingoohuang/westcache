package com.github.bingoohuang.westcache.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import java.net.ServerSocket;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/8.
 */
public class EmbeddedRedis {
    @SneakyThrows
    public static int getRandomPort() {
        @Cleanup val socket = new ServerSocket(0);
        return socket.getLocalPort();
    }

    public static final int port1 = getRandomPort();
    public static final int port2 = getRandomPort();

    private RedisServer redis1;
    private RedisServer redis2;

    @SneakyThrows
    public EmbeddedRedis() {
        redis1 = new RedisServer(port1);
        redis2 = new RedisServer(port2);
        redis1.start();
        redis2.start();

        Redis.setJedis(new Jedis("127.0.0.1", port1));
    }

    public void stop() {
        redis1.stop();
        redis2.stop();
    }
}
