package com.createpen.fifteenhundun.module.food.service;

import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;

public interface FoodFinanceService {
    /**
     * 插入饮食费用记录
     * @param dto 饮食费用DTO
     * @return 1-成功，0-已存在
     */
    Integer insertFoodMoney(DayFoodPriceDTO dto);

    /**
     * 更新饮食费用记录
     * @param dto 饮食费用DTO
     */
    void updateFoodMoney(DayFoodPriceDTO dto);
}
