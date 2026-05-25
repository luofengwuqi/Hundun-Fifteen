package com.createpen.fifteenhundun.module.sleep.service;

import com.createpen.fifteenhundun.module.sleep.pojo.DTO.SleepHealthDTO;

public interface SleepHealthService {
    /**
     * @return 1 插入成功，0 当日记录已存在
     */
    int insertSleep(SleepHealthDTO dto);

    void updateSleep(SleepHealthDTO dto);
}
