package com.createpen.fifteenhundun.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    //发 HTTP 请求的Bean AI要用
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}