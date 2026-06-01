package com.createpen.fifteenhundun.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "glm")
public class GlmProperties {
    /**
     * GLM API密钥
     */
    private String apiKey;
    
    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * GLM API地址
     */
    private String apiUrl;

}
