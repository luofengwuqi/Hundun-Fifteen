package com.createpen.fifteenhundun.common.constant.redis;

public class UserModelStatusRedisConstant {
    //用户模块启用状态"user:modelStatus:${userId}"
    public static final String USER_MODEL_STATUS_PATH= "user:modelStatus:";
    //过期时间30min
    public static final Integer USER_MODEL_STATUS_EXPIRE_TIME = 30;
}
