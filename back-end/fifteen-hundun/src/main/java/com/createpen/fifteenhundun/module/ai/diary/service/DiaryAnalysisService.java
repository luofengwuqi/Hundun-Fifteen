package com.createpen.fifteenhundun.module.ai.diary.service;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.ai.diary.client.GlmClient;
import com.createpen.fifteenhundun.module.ai.diary.pojo.DTO.DiaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.createpen.fifteenhundun.module.ai.constant.DiaryPromptConstant.Diary_PROMPT;

@Service
@Slf4j
public class DiaryAnalysisService {
    private final GlmClient glmClient;
    public DiaryAnalysisService(GlmClient glmClient) {
        this.glmClient = glmClient;
    }
    public Result<String> analysis(DiaryDto dto) {
        String result="";
        try{
            result = glmClient.chat(Diary_PROMPT, LocalDateTime.now()+dto.getDiary());
        }catch (Exception e){
            log.error("被AI平台限流了...请稍后再试...");
            return Result.fail("被AI平台限流了...请稍后再试...");
        }
        log.info("分析结果: {}", result);
        String res = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
        return Result.success(res);
    }
}
