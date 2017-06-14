[![Build Status](https://travis-ci.org/bingoohuang/westcache.svg?branch=master)](https://travis-ci.org/bingoohuang/westcache)
[![Coverage Status](https://coveralls.io/repos/github/bingoohuang/westcache/badge.svg?branch=master)](https://coveralls.io/github/bingoohuang/westcache?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bingoohuang/westcache/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.bingoohuang/westcache/)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# westcache
Java annotation based cache, with convenience for cache flush.

## In ideal world, no cache needed.
In the ideal world, everything works well and fast, and there is no need to considerate caching which is in order to make things faster.
So we take the business points and follow task steps, like write a service as following:
```java
@Service
public class UserService {
    @Inject UserDao userDao;
    
    public Map<String, UserMeta> loadUserMetas() {
        List<UserMeta> metas = userDao.selectAllUserMetas();
        Map<String, UserMeta> map = Maps.newHashMap();
        for (UserMeta meta : metas) {
            map.put(meta.getMetaId(), meta);
        }
        return map;
    }
}
```

## You complete your work and search for more efficient.
As you searched the whole code, you find that the user metas are basic data, 
which means the data will no change nearly. But the program read it from the database
time by time even there is no change. You want to optimize it.

```java
@Service
public class UserService {
    @Inject UserDao userDao;
    
    @WestCacheable
    public Map<String, UserMeta> loadUserMetas() {
        List<UserMeta> metas = userDao.selectAllUserMetas();
        Map<String, UserMeta> map = Maps.newHashMap();
        for (UserMeta meta : metas) {
            map.put(meta.getMetaId(), meta);
        }
        return map;
    }
}
```
And then the program runs faster than ever. What's the difference is that
the loadUserMetas method is annotated by @WestCacheable. And then the method will
be only executed at first once, and its result is then put into a guava cache.
When the method is invoked next time, the cached value will be served as its result directly. 
That's the simplest usage of westcache.

## The core concepts of westcache
First, give a brief introduction:

1. manager: the under caching mechanism, like guava, redis, file, expiring. 
2. keyer: the cache key strategy, like simple deal key like XyzService.cacheMethod.
3. snapshot：the storage snapshot for cached values, like file, redis.
4. flusher: the cache flushing mechanism, to trigger cache updating.
5. config: provide some configurations.
6. interceptor：a hood to add some logic around the original method invoke.

## The manager
The function of manager is to take consideration where the cached data should be stored fast and conveniently.
The default manager is based guava cache with the name "default".
The west cache manager should implement the interface WestCacheManager or 
extends the abstract class BaseCacheManager to provide some common operations.
```java
public interface WestCacheManager {
    WestCacheItem get(WestCacheOption option, String cacheKey, Callable<WestCacheItem> callable);

    WestCacheItem get(WestCacheOption option, String cacheKey);

    void put(WestCacheOption option, String cacheKey, WestCacheItem cacheValue);
}
```

## The problem of cache
1. Cache invalidation. Even in distributed deployed environment.
2. Cache flood. Also calls Dog-Piling, cache stampede or thundering herd.
In our experience, there is no need for expire of cache, like expireAfterAccess, expireAfterWrite.
What we want is, if there is only one situation to trigger cache invalidation is when there is data 
maintenance manually.

## how to cache the result to redis
```java
@Service
public class UserService {
    @WestCacheable(manager="redis", keyer="simple")
    public Map<String, UserMeta> loadUserMetas() {
       // ...
    }
}
```
or I want to cache the result to another redis
```java
@Service
public class UserService {
    @WestCacheable(manager="redis", keyer="simple", specs="redisBean=that")
    public Map<String, UserMeta> loadUserMetas() {
       // ...
    }
}

@Component
public class RedisConfig {
    @Bean(name = "that")
    public JedisCommands thatJedisCommands() {
        return Redis.createtJedisCommands( "127.0.0.1", 7379, 10);
    }
}

```

## how to deal global cache like access token in wechat development?
The access token in wechat is a two hours living and limited times for refresh in a day.
We can not refresh time by time. We should cache it.
We can use redis to do this. We try to read it from redis before token api call, 
and store to redis after a wechat token api call.
The interceptor is just used to save this.

```java
@WestCacheable(keyer = "simple", manager = "expiring",
        interceptor = "redis", specs = "expireAfterWrite=2h")
public static class WechatTokenService {
    public String getAccessToken() {
        // call wechat token api;
    }
}

```

## The data format for snapshot or redis caching.
Westcache use [fastjson](https://github.com/alibaba/fastjson) to serialize cached value.
```java
JSON.toJSONString(obj);
```
So you can check the data in redis, it will look like this:
```json
{"addr":"北京","id":2,"name":"dingoo"}
```

## Customized cache annotation support
Conveniently, customized annoation can be defined to make usage more simpler.
```java
@WestCacheable(snapshot = "file", key = "abc")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Ann1 {
    String key() default ""; // key can be defined to be overriding.
}

@Service
public class AnnService {
    @Ann1(key = "mm22") // this method result will be cache with key mm22
    public String sth() {
        return "" + System.currentTimeMillis();
    }
}
```

## Table-based flusher
A table can be maitained to invalidate cache by increment value_version field.
The pre-defined flusher "table" will repeatedly check the value versions every 1 minute.
And when the value version is changed, the related cache will be invalidated.

```sql
--ORACLE SQL:

DROP TABLE WESTCACHE_FLUSHER;
CREATE TABLE WESTCACHE_FLUSHER(
    CACHE_KEY VARCHAR2(2000 BYTE) NOT NULL PRIMARY KEY,
    KEY_MATCH VARCHAR2(20 BYTE) DEFAULT 'full' NOT NULL,
    MATCH_PRI NUMBER DEFAULT 0 NOT NULL,
    VALUE_VERSION NUMBER DEFAULT 0 NOT NULL,
    CACHE_STATE NUMBER DEFAULT 1 NOT NULL,
    VALUE_TYPE VARCHAR2(20 BYTE) DEFAULT 'none' NOT NULL,
    SPECS VARCHAR2(2000 BYTE) NULL,
    DIRECT_VALUE LONG,
    CACHE_REMARK VARCHAR2(200 BYTE) NULL
);

COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_KEY IS 'cache key';
COMMENT ON COLUMN WESTCACHE_FLUSHER.KEY_MATCH IS 'full:full match,prefix:prefix match';
COMMENT ON COLUMN WESTCACHE_FLUSHER.MATCH_PRI IS 'priority to match in descending way';
COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_VERSION IS 'version of cache, increment it to update cache';
COMMENT ON COLUMN WESTCACHE_FLUSHER.DIRECT_VALUE IS 'direct json value for the cache';
COMMENT ON COLUMN WESTCACHE_FLUSHER.CACHE_STATE IS '0 disabled 1 enabled';
COMMENT ON COLUMN WESTCACHE_FLUSHER.VALUE_TYPE IS 'value access type, direct: use direct json in DIRECT_VALUE field';
COMMENT ON COLUMN WESTCACHE_FLUSHER.SPECS IS 'specs for extension';

-- MySql SQL:
DROP TABLE IF EXISTS WESTCACHE_FLUSHER;
CREATE TABLE WESTCACHE_FLUSHER(
    CACHE_KEY VARCHAR(2000) NOT NULL PRIMARY KEY COMMENT 'cache key',
    KEY_MATCH VARCHAR(20) DEFAULT 'full' NOT NULL COMMENT 'full:full match,prefix:prefix match',
    MATCH_PRI TINYINT DEFAULT 0 NOT NULL COMMENT 'priority to match in descending way',
    VALUE_VERSION TINYINT DEFAULT 0 NOT NULL COMMENT 'version of cache, increment it to update cache',
    CACHE_STATE TINYINT DEFAULT 1 NOT NULL COMMENT 'direct json value for the cache',
    VALUE_TYPE VARCHAR(20) DEFAULT 'none' NOT NULL COMMENT 'value access type, direct: use direct json in DIRECT_VALUE field',
    SPECS VARCHAR(2000) NULL COMMENT 'specs for extension',
    DIRECT_VALUE TEXT,
    CACHE_REMARK VARCHAR(200) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

```
Example rows:

|CACHE_KEY|KEY_MATCH|MATCH_PRI|VALUE_VERSION|CACHE_STATE|VALUE_TYPE|SPECS|DIRECT_VALUE|
|---------|---------|---------|----|-----------|----------|-----|------------|
|TitaService.directValue|full|0|1|1|direct|null|"helllo bingoo"|
|TitaService.getCities|prefix|0|1|1|none|null|null|
|TitaService.getCities2|prefix|0|3|1|direct|null|{"@type":"java.util.HashMap","JiangXi":"YYY222","JiangSu":"XXX111"}|
|TitaService.specs|full|0|0|1|direct|readBy=loader;loaderClass=com.github.bingoohuang.westcache.MyLoader|null|
|TitaService.specsRedis|full|0|0|1|direct|readBy=redis|null|

```java
@WestCacheable(keyer = "simple", flusher = "table")
```

## expireAfterWrite=[duration] support
In specs, expireAfterWrite can be set like expireAfterWrite=[duration], 
Durations are represented by an integer, followed by one of "d", "h", "m", or "s",
representing days, hours, minutes, or seconds respectively.
expireAfterWrite will work together with redis cache manager, expiring cache manager or redis interceptor.
```java

public class RedisExpireService {
    @WestCacheable(manager = "redis", keyer = "simple", flusher = "table",
            specs = "expireAfterWrite=2h")
    public SomeBean doSth() {
        // ...
    }

    @@WestCacheable(interceptor = "redis", keyer = "simple", flusher = "table",
                 specs = "expireAfterWrite=1d")
    public OtherBean doOther() {
        // ...
    }
    
    @WestCacheable(manager = "expiring", specs = "expireAfterWrite=10m")
    public String doThree() {
        // ...
    }
}
```

## Quartz Scheduler Flusher
An in-built quartz flusher is supported with name `quartz`, 
to enable it please add quartz lib to the classpath, like:
```xml
<dependency>
    <groupId>org.quartz-scheduler</groupId>
    <artifactId>quartz</artifactId>
    <version>2.2.3</version>
</dependency>
```

And then you can use it the @WestCacheable like:
```java
@Component
public static class QuartzService {
    @WestCacheable( flusher = "quartz",
            specs = "scheduled=Every 1 second")
    public long doWhat() {
        return System.currentTimeMillis();
    }
}
```
The scheduled expression can be in format as followings:

1. `Every 1 minute` 表示每1分钟
1. `Every 30 minutes` 表示每30分钟
1. `Every 10 hours` 表示每10小时
1. `Every 60 seconds` 表示每60秒
1. `At 03:00` 表示每天凌晨3点
1. `At ??:40` 表示每小时的第40分钟
1. `0 20 * * * ?` 表示每小时开始20分钟
1. `Every 30 minutes from 2016-10-10 to 2017-10-12` 表示从2016年10月10日到2017年10月12日之间的每天凌晨3点
1. `At 03:00 to 2013-11-01`
1. `0 20 * * * ? from 2013-10-10 14:00:00`

Or in spring context:
```java
@Configuration @ComponentScan @WestCacheableScan @WestCacheableEnabled
public class SpringConfig {
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        val creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }


    @Bean(name = "cacheFlushScheduledBean")
    public String cacheFlushScheduled() {
        return "Every 1 seconds";
    }
}

@Service 
public class DemoService {
    @Setter String data;

    @WestCacheable(flusher = "quartz",
            specs = "scheduledBean=cacheFlushScheduledBean")
    public long doWhat() {
        return System.currentTimeMillis();
    }
}
```

## The source of name as westcache
Film "West World".

## Build scripts
1. `mvn clean install sonar:sonar -Dsonar.organization=bingoohuang-github -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=a7fe683637d6e1f54e194817cc36e78936d4fe61`

