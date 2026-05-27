package com.createpen.fifteenhundun.common.constant.redis;

public class MoodRedisConstant {
    /** user:health:mood:{userId}:{date} */
    public static final String MOOD_HEALTH_KEY = "user:health:mood:";
    public static final Integer MOOD_HEALTH_EXPIRE = 30 * 60;
}
