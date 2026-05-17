package com.createpen.fifteenhundun.module.food.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodDTO;
import com.createpen.fifteenhundun.module.food.service.FoodHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/food/health")
@RestController
@Slf4j
public class DayFoodHealthController {
    private final FoodHealthService foodHealthService;

    public DayFoodHealthController(FoodHealthService foodHealthService) {
        this.foodHealthService = foodHealthService;
    }

    /**
     * 插入饮食健康记录（吃了什么）
     */
    @PostMapping
    public Result<Integer> insertFood(@RequestBody DayFoodDTO dto) {
        log.info("insertFood");
        Integer flag = foodHealthService.insertFood(dto);
        return Result.success(flag);
    }

    /**
     * 更新饮食健康记录
     */
    @PutMapping
    public Result<Void> updateFood(@RequestBody DayFoodDTO dto) {
        log.info("updateFood");
        foodHealthService.updateFood(dto);
        return Result.success();
    }
}
