package com.createpen.fifteenhundun.module.sleep.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.sleep.pojo.DTO.SleepHealthDTO;
import com.createpen.fifteenhundun.module.sleep.service.SleepHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sleep/health")
@Slf4j
public class SleepHealthController {

    private final SleepHealthService sleepHealthService;

    public SleepHealthController(SleepHealthService sleepHealthService) {
        this.sleepHealthService = sleepHealthService;
    }

    @PostMapping
    public Result<Integer> insertSleep(@RequestBody SleepHealthDTO dto) {
        log.info("insertSleep: {}", dto);
        Integer flag = sleepHealthService.insertSleep(dto);
        return Result.success(flag);
    }

    @PutMapping
    public Result<Void> updateSleep(@RequestBody SleepHealthDTO dto) {
        log.info("updateSleep: {}", dto);
        sleepHealthService.updateSleep(dto);
        return Result.success();
    }
}
