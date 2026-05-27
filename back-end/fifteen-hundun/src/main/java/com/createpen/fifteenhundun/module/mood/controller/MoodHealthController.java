package com.createpen.fifteenhundun.module.mood.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.mood.pojo.DTO.MoodHealthDTO;
import com.createpen.fifteenhundun.module.mood.service.MoodHealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mood/health")
@Slf4j
public class MoodHealthController {

    private final MoodHealthService moodHealthService;

    public MoodHealthController(MoodHealthService moodHealthService) {
        this.moodHealthService = moodHealthService;
    }

    @PostMapping
    public Result<Integer> insertMood(@RequestBody MoodHealthDTO dto) {
        log.info("insertMood: {}", dto);
        Integer flag = moodHealthService.insertMood(dto);
        return Result.success(flag);
    }

    @PutMapping
    public Result<Void> updateMood(@RequestBody MoodHealthDTO dto) {
        log.info("updateMood: {}", dto);
        moodHealthService.updateMood(dto);
        return Result.success();
    }
}
