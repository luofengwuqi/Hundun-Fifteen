package com.createpen.fifteenhundun.module.food.service;

import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;

public interface FoodService  {
    int insertFood(DayFoodDTO dayFood);

    Integer insertFoodMoney(DayFoodPriceDTO dayFoodPrice);

    void updateFoodMoney(DayFoodPriceDTO dto);

    void updateFood(DayFoodDTO dto);
}
