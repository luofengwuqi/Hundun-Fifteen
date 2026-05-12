package com.createpen.fifteenhundun.module.food.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.food.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/food")
@RestController
@Slf4j
public class FoodController {
    private final FoodService foodService;
    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    //吃了什么
    @PostMapping("/health")
    public Result<Integer> insertFood(@RequestBody DayFoodDTO dto) {
        log.info("insertFood");
        Integer flag = foodService.insertFood(dto);
        return Result.success(flag);
    }

    //吃了多少钱
    @PostMapping("/finance")
    public Result<Integer> insertFoodMoney(@RequestBody DayFoodPriceDTO dto) {
        log.info("insertMoney");
        log.info("{}", dto);
        //flag:1 成功 0 失败
        Integer flag = foodService.insertFoodMoney(dto);
        return Result.success(flag);
    }

    @PutMapping("/finance")
    public Result<Integer> updateFoodMoney(@RequestBody DayFoodPriceDTO dto) {
        log.info("updateMoney");
        log.info("{}", dto);
        foodService.updateFoodMoney(dto);
        return Result.success();
    }
}
