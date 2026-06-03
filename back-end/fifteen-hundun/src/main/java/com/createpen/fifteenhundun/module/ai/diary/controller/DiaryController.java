package com.createpen.fifteenhundun.module.ai.diary.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.ai.diary.pojo.DTO.DiaryConfirmRequest;
import com.createpen.fifteenhundun.module.ai.diary.pojo.DTO.DiaryDto;
import com.createpen.fifteenhundun.module.ai.diary.service.DiaryAnalysisService;
import com.createpen.fifteenhundun.module.ai.diary.service.DiaryExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("ai/diary")
@Slf4j
public class DiaryController {
    private final DiaryAnalysisService diaryAnalysisService;
    private final DiaryExecutionService diaryExecutionService;

    public DiaryController(DiaryAnalysisService diaryAnalysisService,
                           DiaryExecutionService diaryExecutionService) {
        this.diaryAnalysisService = diaryAnalysisService;
        this.diaryExecutionService = diaryExecutionService;
    }

    @PostMapping("/analyze")
    public Result<String> analyze(@RequestBody DiaryDto dto) {
        log.info("开始分析日记");
        return diaryAnalysisService.analysis(dto);
    }

    @PostMapping("/confirm")
    public Result<Map<String, Object>> confirm(@RequestBody DiaryConfirmRequest request) {
        log.info("开始确认录入日记分析结果");
        Map<String, Object> results = diaryExecutionService.execute(request);
        return Result.success(results);
    }
}