package com.createpen.fifteenhundun.module.selecter.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.MoodSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.SleepSummaryVo;
import com.createpen.fifteenhundun.module.selecter.pojo.vo.StepsSummaryVo;
import com.createpen.fifteenhundun.module.selecter.service.HealthDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequestMapping("/health")
@RestController
@Slf4j
public class HealthDataController {

    private final HealthDataService healthDataService;

    public HealthDataController(HealthDataService healthDataService) {
        this.healthDataService = healthDataService;
    }

    @GetMapping("/mood")
    public Result<MoodSummaryVo> getMoodSummary(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        log.info("获取心情摘要: start={}, end={}", start, end);
        return Result.success(healthDataService.getMoodSummary(start, end));
    }

    @GetMapping("/sleep")
    public Result<SleepSummaryVo> getSleepSummary(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        log.info("获取睡眠摘要: start={}, end={}", start, end);
        return Result.success(healthDataService.getSleepSummary(start, end));
    }

    @GetMapping("/place/steps")
    public Result<StepsSummaryVo> getStepsSummary(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        log.info("获取步数摘要: start={}, end={}", start, end);
        return Result.success(healthDataService.getStepsSummary(start, end));
    }
}
