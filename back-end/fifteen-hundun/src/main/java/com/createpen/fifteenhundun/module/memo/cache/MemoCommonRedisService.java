package com.createpen.fifteenhundun.module.memo.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.memo.pojo.Entity.MemoCommon;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.MemoRedisConstant.MEMO_CACHE_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.MemoRedisConstant.MEMO_COMMON_ITEM_KEY;
import static com.createpen.fifteenhundun.common.constant.redis.MemoRedisConstant.MEMO_COMMON_LIST_KEY;

@Component
public class MemoCommonRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public MemoCommonRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询列表缓存；未命中返回 null，命中空列表返回空集合
     */
    @SuppressWarnings("unchecked")
    public List<MemoCommon> selectList(Long userId, String completion) {
        String key = buildListKey(userId, completion);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return List.of();
        }
        return (List<MemoCommon>) value;
    }

    public void saveList(Long userId, String completion, List<MemoCommon> list) {
        String key = buildListKey(userId, completion);
        int expire = RedisExpireTimeUtil.getRandomTime(MEMO_CACHE_EXPIRE);
        redisTemplate.opsForValue().set(key, list, expire, TimeUnit.SECONDS);
    }

    public boolean hasItemKey(Long userId, Long id) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildItemKey(userId, id)));
    }

    public MemoCommon selectById(Long userId, Long id) {
        Object value = redisTemplate.opsForValue().get(buildItemKey(userId, id));
        if (value == null) {
            return null;
        }
        MemoCommon memo = (MemoCommon) value;
        if (memo.getId() != null && memo.getId() == -1L) {
            return null;
        }
        return memo;
    }

    public void saveById(Long userId, MemoCommon memo) {
        if (memo == null || memo.getId() == null) {
            return;
        }
        String key = buildItemKey(userId, memo.getId());
        int expire = RedisExpireTimeUtil.getRandomTime(MEMO_CACHE_EXPIRE);
        redisTemplate.opsForValue().set(key, memo, expire, TimeUnit.SECONDS);
    }

    /** 防止缓存穿透：标记该 id 不存在 */
    public void saveEmptyById(Long userId, Long id) {
        MemoCommon empty = new MemoCommon();
        empty.setId(-1L);
        empty.setUserId(userId);
        String key = buildItemKey(userId, id);
        int expire = RedisExpireTimeUtil.getRandomTime(MEMO_CACHE_EXPIRE);
        redisTemplate.opsForValue().set(key, empty, expire, TimeUnit.SECONDS);
    }

    public void deleteById(Long userId, Long id) {
        redisTemplate.delete(buildItemKey(userId, id));
    }

    /** 写操作后失效该用户所有筛选维度的列表缓存 */
    public void evictListCaches(Long userId) {
        redisTemplate.delete(buildListKey(userId, null));
        redisTemplate.delete(buildListKey(userId, "pending"));
        redisTemplate.delete(buildListKey(userId, "done"));
    }

    public void evictAll(Long userId, Long memoId) {
        evictListCaches(userId);
        if (memoId != null) {
            deleteById(userId, memoId);
        }
    }

    private String buildListKey(Long userId, String completion) {
        return MEMO_COMMON_LIST_KEY + userId + ":" + normalizeCompletion(completion);
    }

    private String buildItemKey(Long userId, Long id) {
        return MEMO_COMMON_ITEM_KEY + userId + ":" + id;
    }

    private static String normalizeCompletion(String completion) {
        if (completion == null || completion.isBlank()) {
            return "all";
        }
        return completion.trim().toLowerCase();
    }
}
