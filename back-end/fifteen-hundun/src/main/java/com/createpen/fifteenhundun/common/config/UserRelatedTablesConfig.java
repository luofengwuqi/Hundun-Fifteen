package com.createpen.fifteenhundun.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户相关表配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "user")
public class UserRelatedTablesConfig {
    
    /**
     * 用户相关的表名列表
     */
    private List<String> relatedTables;
}
