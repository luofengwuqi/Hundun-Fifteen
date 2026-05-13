package com.createpen.fifteenhundun.common.constant.redis;

public class FoodRedisConstant {
    //user:health:food:${userid}:${DateTime date}
    public static final String FOOD_HEALTH_KEY = "user:health:food:";
    //过期时间 30*60 秒
    public static final Integer FOOD_HEALTH_EXPIRE = 30*60;
    //user:finance:food:${userid}:${DateTime date}
    public static final String FOOD_FINANCE_KEY = "user:finance:food:";
    //过期时间 30*60 秒
    public static final Integer FOOD_FINANCE_EXPIRE = 30*60;
    
}
