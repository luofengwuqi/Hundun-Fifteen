package com.createpen.fifteenhundun.module.food.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFood;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.FoodRedisConstant.FOOD_HEALTH_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.FoodRedisConstant.FOOD_HEALTH_KEY;

@Component
public class FoodHealthRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public FoodHealthRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 保存每日饮食健康记录到 Redis
     *
     * @param dayFood 每日饮食健康记录对象
     */
    public void save(DayFood dayFood) {
        String key = buildKey(dayFood.getUserId(), dayFood.getDate());
        Integer expire = RedisExpireTimeUtil.getRandomTime(FOOD_HEALTH_EXPIRE); // 解决缓存雪崩
        redisTemplate.opsForValue().set(key, dayFood, expire, TimeUnit.SECONDS);
    }
    
    /**
     * 从 Redis 删除每日饮食健康记录
     *
     * @param userId 用户ID
     * @param date 日期
     */
    public void delete(Long userId, LocalDate date) {
        String key = buildKey(userId, date);
        redisTemplate.delete(key);
    }
    
    /**
     * 从 Redis 获取每日饮食健康记录
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 每日饮食健康记录对象，不存在时返回 null
     */
    public DayFood select(Long userId, LocalDate date) {
        String key = buildKey(userId, date);
        return (DayFood) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 构建 Redis key
     *
     * @param userId 用户ID
     * @param date 日期
     * @return Redis key
     */
    private String buildKey(Long userId, LocalDate date) {
        return FOOD_HEALTH_KEY + userId + ":" + date;
    }
}
