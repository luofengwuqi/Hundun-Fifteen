package com.createpen.fifteenhundun.common.constant.redis;

public class SleepRedisConstant {
    /** user:health:sleep:{userId}:{date} */
    public static final String SLEEP_HEALTH_KEY = "user:health:sleep:";
    public static final Integer SLEEP_HEALTH_EXPIRE = 30 * 60;
}
