-- [setup onerr=resume]
 DROP TABLE IF EXISTS WESTCACHE_FLUSHER;
 CREATE TABLE WESTCACHE_FLUSHER(
  CACHE_KEY VARCHAR(2000) NOT NULL PRIMARY KEY COMMENT 'cache key',
  KEY_MATCH VARCHAR(20) DEFAULT 'full' NOT NULL COMMENT 'full:full match,prefix:prefix match',
  VALUE_VERSION TINYINT DEFAULT 0 NOT NULL COMMENT 'version of cache, increment it to update cache',
  CACHE_STATE TINYINT DEFAULT 1 NOT NULL COMMENT 'direct json value for the cache',
  VALUE_TYPE VARCHAR(20) DEFAULT 'none' NOT NULL COMMENT 'value access type, direct: use direct json in DIRECT_VALUE field',
  DIRECT_VALUE TEXT
 ) ;
