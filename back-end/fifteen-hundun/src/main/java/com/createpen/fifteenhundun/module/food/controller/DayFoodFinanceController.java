package com.createpen.fifteenhundun.module.food.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.food.pojo.DTO.DayFoodPriceDTO;
import com.createpen.fifteenhundun.module.food.service.FoodFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/food/finance")
@RestController
@Slf4j
public class DayFoodFinanceController {
    private final FoodFinanceService foodFinanceService;

    public DayFoodFinanceController(FoodFinanceService foodFinanceService) {
        this.foodFinanceService = foodFinanceService;
    }

    /**
     * 插入饮食费用记录（吃了多少钱）
     */
    @PostMapping
    public Result<Integer> insertFoodMoney(@RequestBody DayFoodPriceDTO dto) {
        log.info("insertMoney");
        log.info("{}", dto);
        // flag:1 成功 0 失败
        Integer flag = foodFinanceService.insertFoodMoney(dto);
        return Result.success(flag);
    }

    /**
     * 更新饮食费用记录
     */
    @PutMapping
    public Result<Integer> updateFoodMoney(@RequestBody DayFoodPriceDTO dto) {
        log.info("updateMoney");
        log.info("{}", dto);
        foodFinanceService.updateFoodMoney(dto);
        return Result.success();
    }
}
