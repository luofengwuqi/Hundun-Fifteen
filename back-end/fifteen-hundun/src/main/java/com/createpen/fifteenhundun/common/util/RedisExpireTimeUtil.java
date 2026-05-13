package com.createpen.fifteenhundun.common.util;

public class RedisExpireTimeUtil {
    //解决雪崩用的随机时间方法
    public static Integer getRandomTime(Integer timeCount){
        //获取当前时间的10%（向上取整）
        int smallTimeCount = (int) Math.ceil(timeCount / 10.0);
        //生成 0 到 smallTimeCount 之间的随机数
        int randomTime = (int) (Math.random() * (smallTimeCount + 1));
        return timeCount+randomTime;
    }
}
