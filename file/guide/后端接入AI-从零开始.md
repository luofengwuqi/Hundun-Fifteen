# 后端接入 AI — 从零开始

> 项目：一五馄饨 (15-Hundun)
> 说明：手把手教你如何在 Spring Boot 后端调用 AI 大模型 API

---

## 第 1 课：AI 调用的本质是什么？

抛开"人工智能"这个玄乎的词，技术上其实就是一件事：

> **你的后端向一个 HTTP 接口发请求，拿到返回的文本。**

```
你的后端  ──POST 请求──→  DeepSeek 服务器
         ←──返回 JSON──
```

就像你的前端调后端的 API 一样——你后端也去调 DeepSeek 的 API，只不过传的不是表单数据，而是 **Prompt（提示词）**。  
```
如果按教学文档的规划，module/ai/ 下的完整分层是这样的：
module/ai/
├── client/                        
│   └── DeepSeekClient.java        ← 调用 DeepSeek API 的"通讯兵"
│
├── controller/                    ← 接收前端请求
│   └── DiaryController.java       ← POST /ai/diary/analyze
│
├── pojo/                          ← 数据传输对象
│   ├── DTO/
│   │   └── DiaryRequestDTO.java   ← 前端传过来的请求体
│   └── VO/
│       └── DiaryAnalysisVO.java   ← 返回给前端的结构化结果
│
└── service/
    ├── DiaryAnalysisService.java  ← 业务逻辑：拼 Prompt → 调 AI → 解析 JSON
    └── DiaryExecutionService.java ← 用户确认后，调用各模块 Service 写入数据库
```

---

## 第 2 课：Spring Boot 怎么发起 HTTP 请求？

你的项目目前**没有**发 HTTP 请求的代码（没有 `RestTemplate`），所以我们需要创建一个。

Spring Boot 3.x 推荐用 `RestTemplate` 或 `WebClient`。咱们用 `RestTemplate`，最简单直观。

---


### Step 1：在 common/config 下创建 RestTemplate 配置

新建文件：`common/config/RestTemplateConfig.java`

```java
package com.createpen.fifteenhundun.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

就这几行。这等于告诉 Spring："帮我准备好一个 `RestTemplate` 对象，哪里需要就注入哪里。"

**💡 学习点**：
- `@Configuration`：标记这是一个配置类
- `@Bean`：把方法返回值注册为一个 Spring Bean，之后在其他类里用构造器注入就能拿到它
- `RestTemplate`：Spring 提供的 HTTP 客户端，用来给 DeepSeek 发请求

---

### Step 2：写一个类，专门调用 DeepSeek API

新建文件：`module/ai/client/DeepSeekClient.java`

```java
package com.createpen.fifteenhundun.module.ai.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DeepSeekClient {

    private final RestTemplate restTemplate;

    // 从 application.yml 读取配置
    @Value("${fifteen-hundun.deepseek.api-key}")
    private String apiKey;

    @Value("${fifteen-hundun.deepseek.api-url}")
    private String apiUrl;

    public DeepSeekClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 调用 DeepSeek 对话 API
     * @param systemPrompt  系统提示词（描述任务）
     * @param userMessage   用户消息（日记内容）
     * @return AI 返回的文本内容
     */
    public String chat(String systemPrompt, String userMessage) {
        // 1. 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);  // Authorization: Bearer sk-xxx

        // 2. 构造请求体（DeepSeek 兼容 OpenAI 格式）
        Map<String, Object> requestBody = Map.of(
            "model", "deepseek-chat",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "temperature", 0.3  // 温度越低，输出越稳定
        );

        // 3. 发送 POST 请求
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        log.info("正在调用 DeepSeek API...");
        ResponseEntity<Map> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            request,
            Map.class
        );

        // 4. 解析响应
        // DeepSeek 响应结构: { choices: [ { message: { content: "..." } } ] }
        Map body = response.getBody();
        if (body != null && body.containsKey("choices")) {
            List<Map> choices = (List<Map>) body.get("choices");
            if (!choices.isEmpty()) {
                Map message = (Map) choices.get(0).get("message");
                String content = (String) message.get("content");
                log.info("DeepSeek API 调用成功，返回长度: {} 字符", content.length());
                return content;
            }
        }
        
        log.error("DeepSeek API 返回格式异常: {}", body);
        throw new RuntimeException("AI 服务返回异常");
    }
}
```

**💡 学习点**：

| 代码 | 为什么 |
|---|---|
| `@Component` | 把这个类注册为 Spring Bean，其他地方可以注入使用 |
| `@Value("${xxx}")` | 从 `application.yml` 读取配置，和你项目里阿里云 OSS 的读取方式一样 |
| `@Slf4j` | Lombok 注解，自动生成 `log` 变量，用于打印日志 |
| `HttpHeaders.setBearerAuth(apiKey)` | DeepSeek API 要求 token 放在 `Authorization: Bearer sk-xxx` 头里 |
| `Map.of("model", "deepseek-chat", ...)` | DeepSeek（兼容 OpenAI 格式）的请求体格式 |
| `temperature: 0.3` | 越低回答越稳定（适合提取信息），越高越有创意（适合写作） |
| `restTemplate.exchange(url, POST, request, Map.class)` | 发请求，把返回的 JSON 解析成 Map |
| `ResponseEntity<Map>` | 泛型 `Map.class` 告诉 Spring 把响应体解析成 Map 对象 |

#### DeepSeek API 请求体结构详解

```json
{
  "model": "deepseek-chat",          // 模型名称（deepseek-chat 是最新版）
  "messages": [                       // 对话消息列表
    {
      "role": "system",              // 系统角色：设定 AI 的身份和任务
      "content": "你是一个生活记录助手..."  // 提示词内容
    },
    {
      "role": "user",                // 用户角色：用户输入
      "content": "今天早上吃了包子..."     // 日记文本
    }
  ],
  "temperature": 0.3                 // 温度参数：0-2，越低越确定
}
```

#### DeepSeek API 响应结构详解

```json
{
  "id": "chatcmpl-xxx",
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "{"food_health": {...}}"  // ← 这就是我们要的内容
      }
    }
  ],
  "usage": {
    "prompt_tokens": 100,     // 输入消耗的 token 数
    "completion_tokens": 50,  // 输出消耗的 token 数
    "total_tokens": 150       // 总消耗（计费依据）
  }
}
```

所以 `DeepSeekClient.chat()` 返回的 `content` 就是 AI 写的文本。

---

### Step 3：在 application.yml 中添加 DeepSeek 配置

先在 `application.yml`（主配置）加占位符：

```yaml
# DeepSeek AI 配置
fifteen-hundun:
  deepseek:
    api-key: ${fifteen-hundun.deepseek.api-key}
    api-url: ${fifteen-hundun.deepseek.api-url}
```

再在 `application-dev.yml`（开发配置）加实际值：

```yaml
fifteen-hundun:
  # ... 已有的 mysql, alioss, redis 配置（不要动它们） ...
  deepseek:
    api-key: sk-your-actual-api-key-here
    api-url: https://api.deepseek.com/chat/completions
```

> **⚠️ 非常重要**：这个 `application-dev.yml` 目前已经包含了阿里云 OSS 的 AccessKey，**本来就不应该提交到 git**。检查一下 `.gitignore` 里有没有排除它，如果没有，需要加上。

---

### Step 4：写一个 Service 调用它

新建文件：`module/ai/service/DiaryAnalysisService.java`

```java
package com.createpen.fifteenhundun.module.ai.service;

import com.createpen.fifteenhundun.module.ai.client.DeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiaryAnalysisService {

    private final DeepSeekClient deepSeekClient;

    public DiaryAnalysisService(DeepSeekClient deepSeekClient) {
        this.deepSeekClient = deepSeekClient;
    }

    /**
     * 分析日记，提取结构化信息
     * @param diaryText  日记内容
     * @param diaryDate  日记日期（如 2026-06-01）
     * @return AI 返回的 JSON 字符串
     */
    public String analyzeDiary(String diaryText, String diaryDate) {
        // 构造系统提示词
        String systemPrompt = buildSystemPrompt();
        
        // 把日记文本和日期拼给 AI
        String userMessage = String.format(
            "日记日期：%s\n\n日记内容：\n%s",
            diaryDate, diaryText
        );

        // 调用 DeepSeek
        return deepSeekClient.chat(systemPrompt, userMessage);
    }

    /**
     * 构造系统提示词（这是最需要反复调优的部分）
     */
    private String buildSystemPrompt() {
        return """
            你是一个生活记录解析助手。请分析以下日记，提取出所有明确提到的生活记录信息。
            只返回 JSON 格式，不要包含其他说明文字。
            
            ## 可提取的记录类型
            
            1. 饮食记录 (food_health)：
               - morningFood: 早餐
               - afternoonFood: 午餐
               - eveningFood: 晚餐
               - elseFood: 其他
            
            2. 餐饮花费 (food_finance)：
               - morningFoodPrice: 早餐花费（数字，元）
               - afternoonFoodPrice: 午餐花费（数字，元）
               - eveningFoodPrice: 晚餐花费（数字，元）
               - elseFoodPrice: 其他花费（数字，元）
            
            ## 要求
            - 只提取日记中明确提到的内容，不要编造
            - 提取不到的字段设为 null
            - 返回严格的 JSON 格式
            """;
    }
}
```

**💡 学习点**：
- `@Service`：和 `@Component` 功能类似，但语义更明确——这是业务逻辑层
- `String.format()`：Java 的字符串模板，`%s` 是占位符
- `""" ... """`：Java 15+ 的文本块语法，写多行字符串不用拼接了

---

### Step 5：写一个 Controller，暴露给前端调

新建文件：`module/ai/controller/DiaryController.java`

```java
package com.createpen.fifteenhundun.module.ai.controller;

import com.createpen.fifteenhundun.common.response.Result;
import com.createpen.fifteenhundun.module.ai.service.DiaryAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/ai/diary")
@Slf4j
public class DiaryController {

    private final DiaryAnalysisService diaryAnalysisService;

    public DiaryController(DiaryAnalysisService diaryAnalysisService) {
        this.diaryAnalysisService = diaryAnalysisService;
    }

    @PostMapping("/analyze")
    public Result<String> analyzeDiary(@RequestBody Map<String, String> request) {
        String diaryText = request.get("text");
        String diaryDate = request.getOrDefault("date", LocalDate.now().toString());

        log.info("收到日记分析请求，日期: {}, 长度: {} 字", diaryDate, diaryText.length());
        
        String result = diaryAnalysisService.analyzeDiary(diaryText, diaryDate);
        return Result.success(result);
    }
}
```

**💡 学习点**：
- `@RestController`：标记这是一个控制器，所有方法返回 JSON
- `@RequestMapping("/ai/diary")`：这个控制器的 URL 前缀
- `@RequestBody Map<String, String>`：直接把请求体 JSON 解析成 Map
- `request.getOrDefault("date", ...)`：如果没传 date 参数，就用当天日期
- `Result.success()`：你的项目统一的响应封装

---

## 第 3 课：验证一切是否通顺

### 启动项目

```bash
cd back-end/fifteen-hundun
mvn spring-boot:run
```

### 用 curl 测试

因为你的项目有 JWT 拦截器，需要先登录拿到 token：

```bash
# Step 1：登录拿到 token
curl -X POST http://localhost:5757/user/login/username-password \
  -H "Content-Type: application/json" \
  -d '{"username": "你的用户名", "password": "你的密码"}'

# 从返回结果中复制 token（data 字段的值）

# Step 2：用 token 调用 AI 接口
curl -X POST http://localhost:5757/ai/diary/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer 这里填你复制的token" \
  -d '{"text": "今天早上吃了两个包子和一碗粥，花了12块钱。下午去公园跑了3公里，心情不错。"}'
```

### 如果返回 401 未授权

找到 JWT 拦截器，把 `/ai/**` 加到白名单。

JWT 拦截器的文件可能在：`framework/interception/LoginFilter.java` 或类似位置。

```java
// 在已有的白名单数组里加上 "/ai/**"
String[] ALLOWED_PATHS = {
    "/user/login/**",
    "/user/register",
    "/ai/**",          // ← 加这一行
    // ... 其他已有的白名单路径
};
```

---

## 第 4 课：常见问题排查

### 问题 1：`java.lang.IllegalArgumentException: 'header' must not be null`

`RestTemplate` 没有正确注入。检查：
- `RestTemplateConfig.java` 是否有 `@Configuration`
- `DeepSeekClient` 构造器参数是否正确

### 问题 2：`java.net.ConnectException: Connection refused`

DeepSeek API 地址不对，或者网络不通：
- 检查 `api-url` 是否 = `https://api.deepseek.com/chat/completions`
- 用 curl 单独测试一下这个地址能不能通

### 问题 3：`401 Authentication Fails`

API Key 不对：
- 检查 `api-key` 是否以 `sk-` 开头
- 去 DeepSeek 开放平台检查 key 是否过期或已删除

### 问题 4：返回的结果是乱码或空字符串

`restTemplate.exchange()` 返回的 Map 解析不对：
- 在 `DeepSeekClient.chat()` 里加一行 `log.info("完整响应: {}", body)` 看原始响应
- 确认响应结构是不是预期的 `{ choices: [{ message: { content: "..." } }] }`

---

## 第 5 课：你接下来需要做什么

以上代码结构已经完整，从开始到跑通的顺序：

### 第 1 步：注册 DeepSeek 账号（2 分钟）
1. 打开 [platform.deepseek.com](https://platform.deepseek.com/)
2. 注册账号
3. 进入 API Keys 页面 → 新建 API key
4. 复制 key（以 `sk-` 开头）

### 第 2 步：测试 API 连通性（2 分钟）

用 curl 直接调 DeepSeek，不经过你的项目，纯粹验证网络和 key：

```bash
curl -X POST https://api.deepseek.com/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer sk-你的key" \
  -d '{
    "model": "deepseek-chat",
    "messages": [
      {"role": "system", "content": "你是一个助手"},
      {"role": "user", "content": "你好，1+1等于几？"}
    ]
  }'
```

如果返回了类似下面这样的内容，就说明 API 连通了：
```json
{
  "choices": [
    {
      "message": {
        "content": "1+1等于2"
      }
    }
  ]
}
```

### 第 3 步：把代码加进项目（15 分钟）
按上面的 Step 1~5 把代码加到你的项目里：
1. `RestTemplateConfig.java`
2. `DeepSeekClient.java`
3. `DiaryAnalysisService.java`
4. `DiaryController.java`
5. 配好 `application.yml` 和 `application-dev.yml`
6. 启动项目

### 第 4 步：测试你的接口（5 分钟）
用 curl 调你的 `/ai/diary/analyze`，看能不能返回结果。

### 第 5 步：调优 Prompt（持续）
尝试不同的日记内容，观察 AI 返回的质量，不断优化 `buildSystemPrompt()` 里的提示词。

---

## 附：项目文件结构总览

完成以上步骤后，新增的文件结构如下：

```
back-end/fifteen-hundun/src/main/java/com/createpen/fifteenhundun/
├── common/
│   └── config/
│       └── RestTemplateConfig.java          ← 新增：HTTP 客户端配置
│
└── module/
    └── ai/                                  ← 新增：AI 模块
        ├── client/
        │   └── DeepSeekClient.java          ← 调用 DeepSeek API
        ├── controller/
        │   └── DiaryController.java         ← REST 接口
        └── service/
            └── DiaryAnalysisService.java    ← 业务逻辑 + Prompt

back-end/fifteen-hundun/src/main/resources/
├── application.yml                          ← 修改：加 deepseek 占位符
└── application-dev.yml                      ← 修改：加 deepseek 实际值
```
