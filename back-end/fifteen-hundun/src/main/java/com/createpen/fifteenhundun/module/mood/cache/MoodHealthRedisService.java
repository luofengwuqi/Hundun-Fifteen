package com.createpen.fifteenhundun.module.mood.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.mood.pojo.Entity.UserMoodHealth;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.MoodRedisConstant.MOOD_HEALTH_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.MoodRedisConstant.MOOD_HEALTH_KEY;

@Component
public class MoodHealthRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public MoodHealthRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(UserMoodHealth record) {
        String key = buildKey(record.getUserId(), record.getDate());
        int expire = RedisExpireTimeUtil.getRandomTime(MOOD_HEALTH_EXPIRE);
        redisTemplate.opsForValue().set(key, record, expire, TimeUnit.SECONDS);
    }

    public void delete(Long userId, LocalDate date) {
        redisTemplate.delete(buildKey(userId, date));
    }

    public UserMoodHealth select(Long userId, LocalDate date) {
        return (UserMoodHealth) redisTemplate.opsForValue().get(buildKey(userId, date));
    }

    private String buildKey(Long userId, LocalDate date) {
        return MOOD_HEALTH_KEY + userId + ":" + date;
    }
}
