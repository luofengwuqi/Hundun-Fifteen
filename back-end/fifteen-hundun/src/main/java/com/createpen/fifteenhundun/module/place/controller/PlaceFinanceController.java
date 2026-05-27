package com.createpen.fifteenhundun.module.place.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceFinanceDTO;
import com.createpen.fifteenhundun.module.place.service.PlaceFinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/place/finance")
@Slf4j
public class PlaceFinanceController {

    private final PlaceFinanceService placeFinanceService;

    public PlaceFinanceController(PlaceFinanceService placeFinanceService) {
        this.placeFinanceService = placeFinanceService;
    }

    @PostMapping
    public Result<Integer> insertPlaceFinance(@RequestBody PlaceFinanceDTO dto) {
        log.info("insertPlaceFinance: {}", dto);
        Integer flag = placeFinanceService.insertPlaceFinance(dto);
        return Result.success(flag);
    }

    @PutMapping
    public Result<Void> updatePlaceFinance(@RequestBody PlaceFinanceDTO dto) {
        log.info("updatePlaceFinance: {}", dto);
        placeFinanceService.updatePlaceFinance(dto);
        return Result.success();
    }
}
