package com.createpen.fifteenhundun.module.mood.service;

import com.createpen.fifteenhundun.module.mood.pojo.DTO.MoodHealthDTO;

public interface MoodHealthService {
    /**
     * @return 1 插入成功，0 当日记录已存在
     */
    int insertMood(MoodHealthDTO dto);

    void updateMood(MoodHealthDTO dto);
}
