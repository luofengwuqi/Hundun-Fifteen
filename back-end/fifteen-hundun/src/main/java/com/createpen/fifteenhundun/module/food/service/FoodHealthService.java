package com.createpen.fifteenhundun.module.food.service;

import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;

public interface FoodHealthService {
    /**
     * 插入饮食健康记录
     * @param dto 饮食健康DTO
     * @return 1-成功，0-已存在
     */
    int insertFood(DayFoodDTO dto);

    /**
     * 更新饮食健康记录
     * @param dto 饮食健康DTO
     */
    void updateFood(DayFoodDTO dto);
}
