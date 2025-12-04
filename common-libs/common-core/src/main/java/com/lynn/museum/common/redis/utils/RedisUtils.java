package com.lynn.museum.common.redis.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 通用版本
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 重试配置
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_INTERVAL_MS = 100;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 执行 Redis 操作，支持自动重试
     */
    private <T> T executeWithRetry(RedisOperation<T> operation, T defaultValue) {
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < MAX_RETRY_TIMES) {
            try {
                return operation.execute();
            } catch (QueryTimeoutException | RedisConnectionFailureException e) {
                lastException = e;
                retryCount++;
                
                if (retryCount < MAX_RETRY_TIMES) {
                    log.warn("[common-redis] Redis操作超时，正在重试 ({}/{}): {}", 
                            retryCount, MAX_RETRY_TIMES, e.getMessage());
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("[common-redis] Redis操作失败，已达最大重试次数", e);
                }
            } catch (Exception e) {
                log.error("[common-redis] Redis操作异常", e);
                throw e;
            }
        }
        
        if (lastException != null) {
            log.error("[common-redis] Redis操作最终失败: {}", lastException.getMessage());
        }
        return defaultValue;
    }
    
    @FunctionalInterface
    private interface RedisOperation<T> {
        T execute();
    }

    // =============================common============================

    /**
     * 指定缓存失效时间
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置缓存失效时间异常", e);
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("判断key是否存在异常", e);
            return false;
        }
    }

    /**
     * 删除缓存
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(List.of(key));
            }
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置缓存异常", e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间（支持重试）
     */
    public boolean set(String key, Object value, long time) {
        return executeWithRetry(() -> {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        }, false);
    }

    /**
     * 递增
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================================Map=================================

    /**
     * HashGet
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("设置Hash缓存异常", e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("设置Hash缓存异常", e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("设置Hash缓存异常", e);
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("设置Hash缓存异常", e);
            return false;
        }
    }

    /**
     * 删除hash表中的值
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取Set缓存异常", e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("判断Set中是否存在值异常", e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("设置Set缓存异常", e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            log.error("设置Set缓存异常", e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("获取Set大小异常", e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error("移除Set值异常", e);
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取List缓存异常", e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取List大小异常", e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("获取List索引值异常", e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置List缓存异常", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("设置List缓存异常", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置List缓存异常", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("设置List缓存异常", e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("更新List索引值异常", e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            log.error("移除List值异常", e);
            return 0;
        }
    }

    /**
     * 分布式锁
     */
    public boolean tryLock(String key, String value, long expireTime) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("获取分布式锁异常", e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     */
    public boolean releaseLock(String key, String value) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(key), value);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("释放分布式锁异常", e);
            return false;
        }
    }
}
