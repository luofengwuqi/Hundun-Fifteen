package com.createpen.fifteenhundun.module.place.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.place.pojo.DTO.PlaceHealthDTO;
import com.createpen.fifteenhundun.module.place.service.PlaceHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/place/health")
@Slf4j
public class PlaceHealthController {

    private final PlaceHealthService placeHealthService;

    public PlaceHealthController(PlaceHealthService placeHealthService) {
        this.placeHealthService = placeHealthService;
    }

    @PostMapping
    public Result<Integer> insertPlace(@RequestBody PlaceHealthDTO dto) {
        log.info("insertPlace: {}", dto);
        Integer flag = placeHealthService.insertPlace(dto);
        return Result.success(flag);
    }

    @PutMapping
    public Result<Void> updatePlace(@RequestBody PlaceHealthDTO dto) {
        log.info("updatePlace: {}", dto);
        placeHealthService.updatePlace(dto);
        return Result.success();
    }
}
