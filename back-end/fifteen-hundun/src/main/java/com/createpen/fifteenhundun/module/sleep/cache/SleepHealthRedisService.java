package com.createpen.fifteenhundun.module.sleep.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.sleep.pojo.Entity.UserSleepHealth;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.SleepRedisConstant.SLEEP_HEALTH_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.SleepRedisConstant.SLEEP_HEALTH_KEY;

@Component
public class SleepHealthRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public SleepHealthRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(UserSleepHealth record) {
        String key = buildKey(record.getUserId(), record.getDate());
        int expire = RedisExpireTimeUtil.getRandomTime(SLEEP_HEALTH_EXPIRE);
        redisTemplate.opsForValue().set(key, record, expire, TimeUnit.SECONDS);
    }

    public void delete(Long userId, LocalDate date) {
        redisTemplate.delete(buildKey(userId, date));
    }

    public UserSleepHealth select(Long userId, LocalDate date) {
        return (UserSleepHealth) redisTemplate.opsForValue().get(buildKey(userId, date));
    }

    private String buildKey(Long userId, LocalDate date) {
        return SLEEP_HEALTH_KEY + userId + ":" + date;
    }
}
