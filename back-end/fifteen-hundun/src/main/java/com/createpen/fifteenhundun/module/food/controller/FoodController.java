package com.createpen.fifteenhundun.module.food.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.food.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController("/eat")
@Slf4j
public class FoodController {
    private final FoodService foodService;
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    //吃了什么
    @PostMapping("/health")
    public Result<Void> insertFood(@RequestBody DayFoodDTO dto) {
        log.info("insertFood");
        foodService.insertFood(dto);
        return Result.success();
    }

    //吃了多少钱
    @PostMapping("/money")
    public Result<Void> insertFoodMoney(@RequestBody DayFoodPriceDTO dto) {
        log.info("insertMoney");
        foodService.insertFoodMoney(dto);
        return Result.success();
    }
}
