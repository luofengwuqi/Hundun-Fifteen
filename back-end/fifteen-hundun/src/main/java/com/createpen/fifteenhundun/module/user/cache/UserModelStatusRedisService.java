package com.createpen.fifteenhundun.module.user.cache;

import com.createpen.fifteenhundun.module.user.pojo.Entity.UserModelStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import static com.createpen.fifteenhundun.common.constant.redis.UserModelStatusRedisConstant.USER_MODEL_STATUS_EXPIRE_TIME;
import static com.createpen.fifteenhundun.common.constant.redis.UserModelStatusRedisConstant.USER_MODEL_STATUS_PATH;

@Component
public class UserModelStatusRedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public UserModelStatusRedisService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 删除Redis中的用户模型状态
     *
     * @param userId 用户ID
     */
    public void deleteUMSinRedis(Long userId) {
        if (userId == null) {
            return;
        }
        String key = USER_MODEL_STATUS_PATH + userId;
        stringRedisTemplate.delete(key);
    }
    
    /**
     * 将用户模型状态存入Redis
     *
     * @param userId 用户ID
     * @param userModelStatus 用户模型状态对象
     */
    public void insertUMSinRedis(Long userId, UserModelStatus userModelStatus) {
        if (userId == null || userModelStatus == null) {
            return;
        }
        try {
            String key = USER_MODEL_STATUS_PATH + userId;
            String jsonValue = objectMapper.writeValueAsString(userModelStatus);
            stringRedisTemplate.opsForValue().set(key, jsonValue, USER_MODEL_STATUS_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("保存用户模型状态到Redis失败", e);
        }
    }

    /**
     * 从Redis中获取用户模型状态
     *
     * @param userId 用户ID
     * @return 用户模型状态对象，不存在则返回null
     */
    public UserModelStatus selectUMSinRedis(Long userId) {
        if (userId == null) {
            return null;
        }
        String key = USER_MODEL_STATUS_PATH + userId;
        String jsonValue = stringRedisTemplate.opsForValue().get(key);
        if (jsonValue == null) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonValue, UserModelStatus.class);
        } catch (Exception e) {
            throw new RuntimeException("从Redis中获取用户模型状态失败", e);
        }
    }
}
