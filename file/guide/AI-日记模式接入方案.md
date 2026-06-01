# AI 日记模式接入方案

> 项目：一五馄饨 (15-Hundun)
> 日期：2026-06-01
> 说明：双模录入系统 — 表单模式（已有）+ 日记 AI 模式（待开发）

---

## 一、需求概述

实现**双模录入系统**：

| 模式 | 说明 | 状态 |
|---|---|---|
| **表单模式** | 现有的结构化表单填写（饮食、花费、心情、睡眠等） | ✅ 已有 |
| **日记 AI 模式** | 用户写一篇自由日记，AI 自动识别内容关联的模块，提取结构化数据，调用后端 API 完成录入 | 🆕 待开发 |

**典型场景**：用户写"今天早上吃了两个包子和一碗粥，花了12块钱。下午去公园跑了3公里，心情不错。晚上要记得买牙膏。"

AI 应自动识别并拆解为：
- **饮食健康**：早餐吃了包子和粥
- **餐饮花费**：早餐花了 12 元
- **外出活动**：跑步 3 公里 / 步行 5000 步
- **心情**：心情不错
- **备忘**：买牙膏

---

## 二、免费 AI 模型选择

| 方案 | 成本 | 运行方式 | 中文能力 | 推荐理由 |
|---|---|---|---|---|
| **🟢 DeepSeek API** | 几乎免费（¥1/100万token） | 云端调用 | ⭐⭐⭐⭐⭐ | 一条日记≈0.001元，无需服务器，效果最好 |
| **🟢 通义千问 API** | 新用户送 200万 token/月 | 云端调用 | ⭐⭐⭐⭐ | 阿里系，与OSS同生态，有免费额度 |
| **🟡 Ollama + Qwen2.5(7B)** | 完全免费 | 本地运行 | ⭐⭐⭐ | 需要本地算力，纯CPU也能跑但慢 |
| **🟢 智谱 GLM API** | 新用户送 500万 token | 云端调用 | ⭐⭐⭐⭐ | 免费额度多 |

> **建议**：先用 **DeepSeek API**。注册就送额度，一条日记分析成本不到 0.01 元，在个人使用量级上基本等于免费。中文处理效果是目前国产模型里最好的之一。

---

## 三、整体架构图

```
┌────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                          │
│  ┌──────────┐   ┌──────────────┐   ┌────────────────────┐  │
│  │ 日记输入  │ → │ 结果审核面板  │ → │ 确认录入 → 调API   │  │
│  └──────────┘   └──────────────┘   └────────────────────┘  │
└──────────────────────┬─────────────────────────────────────┘
                       │ POST /ai/diary/analyze
                       ▼
┌────────────────────────────────────────────────────────────┐
│                     后端 (Spring Boot)                      │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │              DiaryController                        │    │
│  │  POST /ai/diary/analyze → 接收日记文本              │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       ▼                                     │
│  ┌────────────────────────────────────────────────────┐    │
│  │           DiaryAnalysisService                      │    │
│  │  ① 构造 Prompt（描述所有模块的数据结构）             │    │
│  │  ② 调用 LLM API → 拿到 JSON 结果                    │    │
│  │  ③ 解析/校验 JSON 格式                              │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       ▼                                     │
│              ┌──────────────────┐                           │
│              │   LlmClient      │ ← 封装 HTTP 调用 LLM      │
│              │  (DeepSeek API)  │                           │
│              └──────────────────┘                           │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │           DiaryExecutionService                     │    │
│  │  (用户确认后调用)                                    │    │
│  │  ① 遍历 AI 提取出的每条记录                          │    │
│  │  ② 调用现有 Module Service 完成入库                  │    │
│  │  ③ 返回每条记录的录入结果                            │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────┬─────────────────────────────────────┘
                       │ 内部调用
                       ▼
              各 Module Service (已有)
         food / memo / mood / sleep / place / payment
```

---

## 四、核心设计：Prompt 工程

这是整个功能**最关键的环节**。后端需要构造一份 Prompt，让 AI 理解所有模块的数据结构，从日记中提取信息。

### Prompt 核心模板

```
你是一个生活记录解析助手。请分析以下日记，提取出所有明确提到的生活记录信息。

## 可提取的记录类型

1. 饮食记录 (food_health)：
   - morningFood: 早餐吃了什么（字符串）
   - afternoonFood: 午餐吃了什么（字符串）
   - eveningFood: 晚餐吃了什么（字符串）
   - elseFood: 其他时间吃了什么（字符串）

2. 餐饮花费 (food_finance)：
   - morningFoodPrice: 早餐花费（数字，元）
   - afternoonFoodPrice: 午餐花费（数字，元）
   - eveningFoodPrice: 晚餐花费（数字，元）
   - elseFoodPrice: 其他花费（数字，元）

3. 睡眠记录 (sleep_health)：
   - quality: 睡眠质量（0=差，1=一般，2=好）
   - durationMin: 睡眠时长（分钟）

4. 心情记录 (mood_health)：
   - mood: 心情指数（0=很差，1=差，2=一般，3=好，4=很好）
   - note: 心情备注（字符串）

5. 外出活动 (place_health)：
   - location: 地点（字符串）
   - transportMode: 交通方式（字符串）
   - steps: 步数（数字）

6. 交通花费 (place_finance)：
   - transportCost: 交通费用（数字，元）
   - transportMode: 交通方式（字符串）
   - location: 地点（字符串）

7. 消费记录 (payment)：数组
   - name: 商品/服务名称
   - price: 价格（元）
   - type: 类型（0=未指定，1=生活/健康，2=学习/投资，3=社交/娱乐，4=责任/应急）

8. 待办事项 (memo_common)：数组
   - text: 内容
   - beginTime: 开始时间
   - endTime: 截止时间

9. 重要备忘 (memo_important)：数组
   - text: 内容

## 要求
- 只提取日记中明确提到的内容，不要编造
- 提取不到的字段设为 null
- "下午/中午/晚上"等时间词，结合日记日期推断具体时间
- 返回严格的 JSON 格式
```

### 示例输出

输入日记：
> "今天早上吃了两个包子和一碗粥，花了12块钱。下午去公园跑了3公里，心情不错。晚上要记得买牙膏。"

期望 AI 返回的 JSON：

```json
{
  "date": "2026-06-01",
  "food_health": {
    "morningFood": "包子、粥",
    "afternoonFood": null,
    "eveningFood": null,
    "elseFood": null
  },
  "food_finance": {
    "morningFoodPrice": 12,
    "afternoonFoodPrice": null,
    "eveningFoodPrice": null,
    "elseFoodPrice": null
  },
  "sleep_health": null,
  "mood_health": {
    "mood": 3,
    "note": null
  },
  "place_health": {
    "location": "公园",
    "transportMode": "跑步",
    "steps": 3000
  },
  "place_finance": null,
  "payment": null,
  "memo_common": [
    {
      "text": "买牙膏",
      "beginTime": "2026-06-01 20:00",
      "endTime": null
    }
  ],
  "memo_important": null
}
```

---

## 五、分步实现计划（MVP 版）

### Step 1：后端 — 搭建 AI 基础模块

新建 `module/ai/` 目录，包含：

```
module/ai/
├── controller/
│   └── DiaryController.java              # POST /ai/diary/analyze
├── service/
│   ├── DiaryAnalysisService.java         # 调用 LLM API + 解析结果
│   └── DiaryExecutionService.java        # 将结构化数据分发到各模块
├── client/
│   └── LlmClient.java                    # 封装 DeepSeek API 调用
└── pojo/
    ├── DiaryRequestDTO.java              # 请求：日记文本 + 日期
    └── DiaryAnalysisResult.java          # AI 返回的结构化结果
```

**LlmClient.java 要做的事**：
- 用 `RestTemplate` 调用 DeepSeek API
- 请求地址：`POST https://api.deepseek.com/chat/completions`
- 传入 System Prompt + 用户日记
- 提取 `choices[0].message.content`（即 JSON 字符串）
- API Key 放在 `application.yml` 的环境变量中（参考阿里云 OSS 的处理方式）

**DiaryAnalysisService.java 要做的事**：
1. 构造 System Prompt（包含上述所有模块的数据结构）
2. 调用 LlmClient 发送请求
3. 用 Jackson 反序列化 JSON 为 `DiaryAnalysisResult` 对象
4. 校验 JSON 格式正确性（字段是否存在、类型是否正确）
5. 如果 AI 返回格式错误，重试一次或返回友好错误信息

### Step 2：后端 — 实现 DiaryExecutionService

用户在前端审核确认后，前端发送确认请求：

```json
{
  "date": "2026-06-01",
  "confirmed": {
    "food_health": { "morningFood": "包子、粥", ... },
    "food_finance": { "morningFoodPrice": 12, ... },
    "mood_health": { "mood": 3, "note": null },
    "place_health": { "location": "公园", "transportMode": "跑步", "steps": 3000 },
    "memo_common": [{ "text": "买牙膏", "beginTime": "2026-06-01 20:00", "endTime": null }]
  }
}
```

`DiaryExecutionService` 遍历 `confirmed` 中的每个字段，**直接调用已有的 Service 方法**（不走 HTTP，注入 Service Bean）：

| 字段 | 调用目标 |
|---|---|
| `food_health` | `DayFoodHealthService.insertDayFood(dto)` |
| `food_finance` | `DayFoodFinanceService.insertDayFoodPrice(dto)` |
| `sleep_health` | `SleepHealthService.insertSleepHealth(dto)` |
| `mood_health` | `MoodHealthService.insertMoodHealth(dto)` |
| `place_health` | `PlaceHealthService.insertPlaceHealth(dto)` |
| `place_finance` | `PlaceFinanceService.insertPlaceFinance(dto)` |
| `payment` | `PaymentService.insertPayments(list)` |
| `memo_common` | `MemoCommonService.insertMemoCommon(dto)` |
| `memo_important` | `MemoImportantService.insertMemoImportant(dto)` |

> **优点**：整个确认录入过程在一个事务中完成，要么全部成功，要么全部回滚。

### Step 3：前端 — 日记输入界面

在 `/user/insert` 页面增加 Tab 切换：

```
┌─────────────────────────────────────┐
│  📋 表单录入    ✍️ 日记录入           │  ← Tab 切换
└─────────────────────────────────────┘
```

日记录入界面包含：
- 一个**大文本域**，让用户自由写日记
- 一个**日期选择器**（默认今天）
- **"AI 分析"按钮**
- 加载状态（加载动画或进度提示）

相关文件：
- `src/views/user/component/Insert/DiaryInput.vue`（新文件）
- 修改 `Insert.vue` 增加 Tab 切换逻辑
- 修改 `InsertRightSide.vue` 支持展示日记模式

### Step 4：前端 — 审核面板

AI 返回结果后，按模块展示卡片列表，每个卡片可独立操作：

```
┌─ 🍜 饮食记录 ──────────────────────┐
│  早餐：包子、粥                     │
│  午餐：—                            │
│  [✏️ 编辑] [🗑️ 删除] [✅ 确认]     │
└────────────────────────────────────┘
┌─ 😊 心情 ──────────────────────────┐
│  心情：好 😊                       │
│  备注：天气不错                    │
│  [✏️ 编辑] [🗑️ 删除] [✅ 确认]     │
└────────────────────────────────────┘
```

- **编辑**：弹出对应模块的编辑对话框，修改字段值
- **删除**：移除这条记录（不录入）
- **确认**：标记为待录入（默认已确认）

底部有 **"全部录入"** 按钮，点击后调用后端录入接口。

### Step 5：新增 API 文件

前端需要新增 API 模块：
- `src/api/ai.js` — 封装日记分析和确认录入的请求

### Step 6：联调与 Prompt 优化

典型测试用例：

| 日记内容 | 期望提取结果 |
|---|---|
| "今天早上吃了两个包子和一碗粥，花了12块" | food_health + food_finance |
| "昨晚睡了8个小时，质量不错" | sleep_health（quality=2, durationMin=480） |
| "下午去公园跑了3公里，走了5000步" | place_health（steps=5000） |
| "今天心情很好，阳光明媚" | mood_health（mood=4） |
| "买了本书花了99，晚上记得写作业" | payment + memo_common |
| "早餐三明治15块，地铁去公司3块，晚上健身1小时" | 多模块混合 |

---

## 六、现有录入 API 速查表

### 后端已有 POST 接口

| 模块 | 端点 | 请求体字段 |
|---|---|---|
| 饮食健康 | `POST /food/health` | `morningFood`, `afternoonFood`, `eveningFood`, `elseFood` |
| 餐饮花费 | `POST /food/finance` | `morningFoodPrice`, `afternoonFoodPrice`, `eveningFoodPrice`, `elseFoodPrice` |
| 睡眠 | `POST /sleep/health` | `quality`（0-2）, `durationMin`（分钟） |
| 心情 | `POST /mood/health` | `mood`（0-4）, `note`（最多500字） |
| 外出 | `POST /place/health` | `location`, `transportMode`, `steps` |
| 交通花费 | `POST /place/finance` | `transportCost`, `transportMode`, `location` |
| 消费 | `POST /payment` | `[{ name, price, type }]`（数组） |
| 普通备忘 | `POST /memo/common` | `text`, `beginTime`, `endTime`, `status`, `remindBeforeHours` |
| 重要备忘 | `POST /memo/important` | `text` |

> 所有接口都自动从 JWT Token 获取 `userId`，日期默认为当天。部分接口有去重逻辑（同一天已存在记录时会返回 0，需要调 PUT 覆盖）。

---

## 七、不踩坑建议

### 1. Prompt 要持续迭代
一开始 AI 提取不准确是正常的。每次发现提取错误，就把这个 case 加到测试集，优化 Prompt 后再试。建议维护一份测试用例清单。

### 2. JSON 校验要严格
AI 偶尔会返回格式错误的 JSON（缺少括号、字段名不对）。后端要做好兜底：
- 用 Jackson 的 `FAIL_ON_MISSING_CREATOR_PROPERTIES` 等配置
- 解析失败时提示 AI 重新生成，或返回友好错误信息

### 3. 日记日期 vs 记录日期
用户可能写"昨天去了公园"，需要在 Prompt 中让 AI 结合日记日期推断每条记录的实际日期。

### 4. 先实现核心 3-4 个模块
不用一开始就支持全部 9 种记录类型。建议从 **饮食 + 花费 + 心情 + 备忘** 这四个最常用的做起，跑通流程后再逐步扩展。

### 5. API Key 安全管理
DeepSeek API Key 放在 `application.yml` 的环境变量中（参考阿里云 OSS key 的处理方式），**永远不要提交到 git**。

### 6. 事务一致性
用户确认录入时，多条记录应在同一个事务中写入。如果某条记录写入失败，应全部回滚并告知用户。

---

## 八、前置准备

在开始编码前，可以先完成以下准备工作：

1. **注册 DeepSeek API**：前往 [platform.deepseek.com](https://platform.deepseek.com/) 注册账号，领取免费额度
2. **在线测试 Prompt**：用 DeepSeek 的在线 Playground 直接测试 Prompt 效果，验证能否从日记中正确提取信息
3. **整理数据字典**：将各模块的字段整理成一份 JSON Schema 文档，后续写 Prompt 和代码时都要用
4. **确认环境**：检查后端是否已引入 `RestTemplate` 或 `WebClient` 依赖（Spring Boot 自带，无需额外引入）
