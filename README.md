# westcache
java cache with single-point administration

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
3. snapshot：the storage snapshot for cached values， like file, redis.
4. flusher: the cache flushing mechanism， to trigger cache updating.
5. config: provide some configurations.
6. interceptor：a hood to add some logic around the original method invoke.

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


## The source of name as westcache
Film "West World".