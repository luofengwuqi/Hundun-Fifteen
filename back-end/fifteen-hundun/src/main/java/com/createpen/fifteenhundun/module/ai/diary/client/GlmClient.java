package com.createpen.fifteenhundun.module.ai.diary.client;


import com.createpen.fifteenhundun.common.config.GlmProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GlmClient {
    private final RestTemplate restTemplate;
    private final GlmProperties glmProperties;

    /** 最大重试次数 */
    private static final int MAX_RETRIES = 3;
    /** 重试前等待的初始毫秒数（每次翻倍） */
    private static final long INITIAL_RETRY_DELAY_MS = 3000;

    public GlmClient(RestTemplate restTemplate, GlmProperties glmProperties) {
        this.restTemplate = restTemplate;
        this.glmProperties = glmProperties;
    }

    public String chat(String systemPrompt, String userMessage) {
        // 1. 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(glmProperties.getApiKey());  // Authorization: Bearer sk-xxx

        // 2. 构造请求体（GLM 兼容 OpenAI 格式）
        Map<String, Object> requestBody = Map.of(
                "model", glmProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.2  // 温度越低，输出越稳定
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 3. 带重试的请求
        long delayMs = INITIAL_RETRY_DELAY_MS;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("正在调用 GLM API（第 {}/{} 次）...", attempt, MAX_RETRIES);
                ResponseEntity<Map> response = restTemplate.exchange(
                        glmProperties.getApiUrl(),
                        HttpMethod.POST,
                        request,
                        Map.class
                );

                // 4. 解析响应
                Map<?, ?> body = response.getBody();
                if (body != null && body.containsKey("choices")) {
                    List<?> choices = (List<?>) body.get("choices");
                    if (!choices.isEmpty()) {
                        Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
                        String content = (String) message.get("content");
                        log.info("GLM API 调用成功，返回长度: {} 字符", content.length());
                        return content;
                    }
                }

                log.error("GLM API 返回格式异常: {}", body);
                throw new RuntimeException("AI 服务返回异常");

            } catch (HttpClientErrorException e) {
                // 429 限流：等待后重试
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempt < MAX_RETRIES) {
                    log.warn("GLM API 限流（429），{} 秒后重试（第 {}/{} 次）...",
                            delayMs / 1000, attempt, MAX_RETRIES);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("AI 请求被中断", ie);
                    }
                    delayMs *= 2;  // 指数退避：3s → 6s → 12s
                } else {
                    // 非 429 错误，或已达最大重试次数，直接抛出
                    throw e;
                }
            }
        }

        // 理论上不会走到这里
        throw new RuntimeException("AI 请求失败（已达最大重试次数）");
    }
}
