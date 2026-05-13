package com.createpen.fifteenhundun.module.food.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.food.pojo.Entity.DayFoodPrice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.FoodRedisConstant.FOOD_FINANCE_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.FoodRedisConstant.FOOD_FINANCE_KEY;

@Component
public class FoodFinanceRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public FoodFinanceRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 保存每日饮食费用到 Redis
     *
     * @param dayFoodPrice 每日饮食费用对象
     */
    public void save(DayFoodPrice dayFoodPrice) {
        String key = buildKey(dayFoodPrice.getUserId(), dayFoodPrice.getDate());
        Integer expire = RedisExpireTimeUtil.getRandomTime(FOOD_FINANCE_EXPIRE);//解决失效雪崩
        redisTemplate.opsForValue().set(key, dayFoodPrice, expire , TimeUnit.SECONDS);
    }
    
    /**
     * 从 Redis 删除每日饮食费用
     *
     * @param userId 用户ID
     * @param date 日期
     */
    public void delete(Long userId, LocalDate date) {
        String key = buildKey(userId, date);
        redisTemplate.delete(key);
    }
    
    /**
     * 从 Redis 获取每日饮食费用
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 每日饮食费用对象,不存在时返回 null
     */
    public DayFoodPrice select(Long userId, LocalDate date) {
        String key = buildKey(userId, date);
        return (DayFoodPrice) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 构建 Redis key
     *
     * @param userId 用户ID
     * @param date 日期
     * @return Redis key
     */
    private String buildKey(Long userId, LocalDate date) {
        return FOOD_FINANCE_KEY + userId + ":" + date;
    }
}
