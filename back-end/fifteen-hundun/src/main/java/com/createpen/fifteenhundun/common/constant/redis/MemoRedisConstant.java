package com.createpen.fifteenhundun.common.constant.redis;

/**
 * 备忘模块 Redis Key 与过期时间
 */
public class MemoRedisConstant {

    /** user:memo:common:list:{userId}:{all|pending|done} */
    public static final String MEMO_COMMON_LIST_KEY = "user:memo:common:list:";

    /** user:memo:common:item:{userId}:{id} */
    public static final String MEMO_COMMON_ITEM_KEY = "user:memo:common:item:";

    /** user:memo:important:list:{userId} */
    public static final String MEMO_IMPORTANT_LIST_KEY = "user:memo:important:list:";

    /** user:memo:important:item:{userId}:{id} */
    public static final String MEMO_IMPORTANT_ITEM_KEY = "user:memo:important:item:";

    /** 列表 / 单条缓存过期时间（秒），30 分钟 + 随机偏移防雪崩 */
    public static final Integer MEMO_CACHE_EXPIRE = 30 * 60;
}
