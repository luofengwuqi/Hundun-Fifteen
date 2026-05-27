package com.createpen.fifteenhundun.module.place.cache;

import com.createpen.fifteenhundun.common.util.RedisExpireTimeUtil;
import com.createpen.fifteenhundun.module.place.pojo.Entity.UserPlaceFinance;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.PlaceRedisConstant.PLACE_FINANCE_EXPIRE;
import static com.createpen.fifteenhundun.common.constant.redis.PlaceRedisConstant.PLACE_FINANCE_KEY;

@Component
public class PlaceFinanceRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public PlaceFinanceRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(UserPlaceFinance record) {
        String key = buildKey(record.getUserId(), record.getDate());
        int expire = RedisExpireTimeUtil.getRandomTime(PLACE_FINANCE_EXPIRE);
        redisTemplate.opsForValue().set(key, record, expire, TimeUnit.SECONDS);
    }

    public void delete(Long userId, LocalDate date) {
        redisTemplate.delete(buildKey(userId, date));
    }

    public UserPlaceFinance select(Long userId, LocalDate date) {
        return (UserPlaceFinance) redisTemplate.opsForValue().get(buildKey(userId, date));
    }

    private String buildKey(Long userId, LocalDate date) {
        return PLACE_FINANCE_KEY + userId + ":" + date;
    }
}
