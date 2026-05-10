package com.createpen.fifteenhundun.module.food.service;

import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;

public interface FoodService {
    void insertFood(DayFoodDTO dayFood);

    void insertFoodMoney(DayFoodPriceDTO dayFoodPrice);
}
