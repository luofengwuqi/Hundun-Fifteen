package com.createpen.fifteenhundun.module.memo.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoImportant;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.MemoRedisConstant.MEMO_CACHE_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.MemoRedisConstant.MEMO_IMPORTANT_ITEM_KEY;
import static com.createpen.fifteenhundun.common.constant.redis.MemoRedisConstant.MEMO_IMPORTANT_LIST_KEY;

@Component
public class MemoImportantRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public MemoImportantRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<MemoImportant> selectList(Long userId) {
        String key = buildListKey(userId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return List.of();
        }
        return (List<MemoImportant>) value;
    }

    public void saveList(Long userId, List<MemoImportant> list) {
        String key = buildListKey(userId);
        int expire = RedisExpireTimeUtil.getRandomTime(MEMO_CACHE_EXPIRE);
        redisTemplate.opsForValue().set(key, list, expire, TimeUnit.SECONDS);
    }

    public boolean hasItemKey(Long userId, Long id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildItemKey(userId, id)));
    }

    public MemoImportant selectById(Long userId, Long id) {
        Object value = redisTemplate.opsForValue().get(buildItemKey(userId, id));
        if (value == null) {
            return null;
        }
        MemoImportant memo = (MemoImportant) value;
        if (memo.getId() != null && memo.getId() == -1L) {
            return null;
        }
        return memo;
    }

    public void saveById(Long userId, MemoImportant memo) {
        if (memo == null || memo.getId() == null) {
            return;
        }
        String key = buildItemKey(userId, memo.getId());
        int expire = RedisExpireTimeUtil.getRandomTime(MEMO_CACHE_EXPIRE);
        redisTemplate.opsForValue().set(key, memo, expire, TimeUnit.SECONDS);
    }

    public void saveEmptyById(Long userId, Long id) {
        MemoImportant empty = new MemoImportant();
        empty.setId(-1L);
        empty.setUserId(userId);
        String key = buildItemKey(userId, id);
        int expire = RedisExpireTimeUtil.getRandomTime(MEMO_CACHE_EXPIRE);
        redisTemplate.opsForValue().set(key, empty, expire, TimeUnit.SECONDS);
    }

    public void deleteById(Long userId, Long id) {
        redisTemplate.delete(buildItemKey(userId, id));
    }

    public void evictListCache(Long userId) {
        redisTemplate.delete(buildListKey(userId));
    }

    public void evictAll(Long userId, Long memoId) {
        evictListCache(userId);
        if (memoId != null) {
            deleteById(userId, memoId);
        }
    }

    private String buildListKey(Long userId) {
        return MEMO_IMPORTANT_LIST_KEY + userId;
    }

    private String buildItemKey(Long userId, Long id) {
        return MEMO_IMPORTANT_ITEM_KEY + userId + ":" + id;
    }
}
