package com.createpen.fifteenhundun.module.ai.diary.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.ai.diary.pojo.DTO.DiaryDto;
import com.createpen.fifteenhundun.module.ai.diary.service.DiaryAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai/diary")
@Slf4j
public class DiaryController {
    private final DiaryAnalysisService diaryAnalysisService;

    public DiaryController(DiaryAnalysisService diaryAnalysisService) {
        this.diaryAnalysisService = diaryAnalysisService;
    }

    @PostMapping("/analyze")
    public Result<String> analyze(@RequestBody DiaryDto dto) {
        log.info("开始分析日记");
        return diaryAnalysisService.analysis(dto);
    }
}