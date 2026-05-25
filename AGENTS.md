# AGENTS.md - 一五馄饨项目开发指南

## 项目概述

一个 **管理员--邮箱--用户** 管理系统，本着最大程度同时保障用户自由与程序健壮性的设计原则。项目包含 4 个可按用户启用/禁用的功能模块：健康(Health)、财务(Finance)、知识(Knowledge)、备忘(Memo)。用户角色分三级：普通用户(status=1)、管理员(status=2)、超级管理员(status=3)。

---

## 技术栈

### 后端 (`back-end/fifteen-hundun/`)

- **框架**: Spring Boot 3.5.8 (Java 17)
- **ORM**: MyBatis + MyBatis-Plus 3.5.5 + PageHelper 1.4.7
- **数据库**: MySQL 8.0
- **缓存**: Redis (Spring Cache + Spring Data Redis)
- **认证**: JWT (jjwt 0.11.5)
- **密码**: BCrypt (jbcrypt 0.4)
- **文件存储**: 阿里云 OSS 3.17.1
- **邮件**: JavaMail 1.6.2 (SMTP 验证码)
- **消息队列**: RabbitMQ (Spring AMQP，已引入依赖)
- **AOP**: Spring AOP (自动填充时间戳、操作日志)
- **定时任务**: Spring Scheduling (凌晨2点同步 Redis 文章数据到 MySQL)
- **构建**: Maven 3.9.11

### 前端 (`front-end/hundun-front/`)

- **框架**: Vue 3.5 + Composition API
- **构建**: Vite 7.3
- **状态管理**: Pinia 3.0 (Composition API 风格)
- **路由**: Vue Router 5.0 (HTML5 History 模式)
- **UI**: Element Plus 2.13
- **HTTP**: Axios 1.13
- **CSS 预处理**: Less 4.6
- **Markdown**: marked 18.0
- **包管理器**: pnpm
- **Node 要求**: ^20.19.0 || >=22.12.0

---

## 项目结构

```
15-hundun/
├── back-end/
│   ├── fifteen-hundun/                  # Spring Boot 后端
│   │   ├── src/main/java/com/createpen/fifteenhundun/
│   │   │   ├── FifteenHundunApplication.java   # 主入口 (@EnableCaching, @EnableScheduling, @EnableAsync)
│   │   │   ├── common/                  # 公共组件
│   │   │   │   ├── anno/                #   自定义注解 (@AutoFillTime, @AutoLog)
│   │   │   │   ├── config/              #   配置类 (CORS, Redis, WebSocket, Async)
│   │   │   │   ├── constant/            #   常量定义 (Redis key, 状态码, 类型码)
│   │   │   │   ├── context/             #   BaseContext (ThreadLocal 用户上下文)
│   │   │   │   ├── enums/               #   枚举 (AutoFillTimeType)
│   │   │   │   ├── exception/           #   自定义异常 + GlobalExceptionHandler
│   │   │   │   ├── response/            #   Result 统一响应封装
│   │   │   │   └── util/                #   工具类 (JWT, OSS, 密码, 验证码, 邮件)
│   │   │   ├── framework/               # 框架层
│   │   │   │   ├── aspect/              #   AOP 切面 (自动填充, 操作日志)
│   │   │   │   ├── interception/        #   拦截器 (JWT LoginFilter, SQL 拦截器)
│   │   │   │   └── task/                #   定时任务 (Redis→MySQL 同步, 启动运行器)
│   │   │   └── module/                  # 业务模块 (每个含 controller/mapper/pojo/service)
│   │   │       ├── article/             #   文章模块 (含 Redis 缓存 + Lua 脚本)
│   │   │       ├── food/                #   饮食模块 (健康记录 + 财务记录)
│   │   │       ├── memo/                #   备忘模块 (普通备忘 + 重要备忘)
│   │   │       ├── payment/             #   支付/财务模块
│   │   │       ├── selecter/            #   数据聚合查询模块
│   │   │       └── user/                #   用户模块 (登录/注册/管理/反馈/WebSocket)
│   │   └── src/main/resources/
│   │       ├── application.yml          # 主配置 (环境变量占位符)
│   │       ├── application-dev.yml      # 开发环境配置
│   │       ├── lua/article/             # Redis Lua 脚本 (点赞/踩)
│   │       └── com/createpen/.../mapper/*.xml  # MyBatis XML 映射
│   ├── Dockerfile                       # 多阶段构建
│   ├── Dockerfile.prebuilt              # 预编译 JAR 构建镜像
│   ├── docker-compose.yml               # 完整构建编排
│   ├── docker-compose.prebuilt.yml      # 预编译编排
│   └── nginx.conf                       # Nginx 反向代理配置
├── front-end/
│   └── hundun-front/                    # Vue 3 前端
│       ├── src/
│       │   ├── api/                     # API 接口定义 (user, article, feedback, food, memo*)
│       │   ├── assets/                  # 静态资源 (图片, CSS, 颜色定义)
│       │   ├── router/                  # 路由配置 + 导航守卫
│       │   ├── stores/                  # Pinia 状态管理 (user, article, feedback, memo)
│       │   ├── utils/                   # 工具函数 (axios 实例, OSS 配置, 登录/登出逻辑)
│       │   └── views/                   # 页面组件
│       │       ├── login/               #   登录/注册 (5种登录方式)
│       │       ├── help/                #   帮助页面
│       │       ├── admin/               #   管理员后台 (用户管理/反馈处理/邀请管理员)
│       │       └── user/                #   用户主页 (Home/Info/Insert/Select)
│       ├── vite.config.js               # Vite 配置 (端口5577, API代理)
│       └── package.json
└── file/
    └── hundun.sql                       # 数据库初始化脚本 (11张表)
```

---

## 开发命令

### 后端

```bash
# 运行开发服务器 (端口 5757, 默认 dev profile)
cd back-end/fifteen-hundun
mvn spring-boot:run

# 构建 JAR (跳过测试)
mvn clean package -DskipTests

# 运行生产 JAR
java -jar target/fifteen-hundun-0.0.1-SNAPSHOT.jar
```

### 前端

```bash
# 安装依赖
cd front-end/hundun-front
pnpm install

# 开发服务器 (端口 5577, 代理 /api → localhost:5757)
pnpm dev

# 生产构建
pnpm build

# 预览构建产物
pnpm preview

# Lint (先 oxlint 再 eslint, 自动修复)
pnpm lint

# 格式化 (Prettier)
pnpm format
```

### Docker 部署

```bash
# 完整构建 (包含 Maven 编译阶段)
cd back-end
docker-compose up -d

# 预编译方式 (需先 mvn package 得到 JAR)
docker-compose -f docker-compose.prebuilt.yml up -d
```

Docker 服务：MySQL 8.0 (:3306) + Redis 7 (:6379) + App (:8080)

---

## 架构说明

### 后端分层架构

每个业务模块 (`module/*`) 遵循统一的分层结构：

```
module/xxx/
├── controller/    # REST 控制器，接收请求、参数校验
├── mapper/        # MyBatis Mapper 接口 + XML SQL 映射
├── pojo/
│   ├── DTO/       # 数据传输对象 (请求参数)
│   ├── Entity/    # 数据库实体类
│   └── VO/        # 视图对象 (响应数据)
├── service/       # 业务逻辑接口
│   └── impl/      # 业务逻辑实现
└── cache/         # Redis 缓存服务 (部分模块)
```

**关键横切关注点：**
- `LoginFilter` (JWT 拦截器) → 验证 Token，通过 `BaseContext` (ThreadLocal) 传递用户信息
- `@AutoFillTime` 注解 + AOP → 自动填充 `create_time`/`update_time`
- `@AutoLog` 注解 + AOP → 自动记录操作日志到 `operation_log` 表
- `Result<T>` → 统一 API 响应封装，业务码 `0` 或 `200` 为成功，`401` 为未授权
- `GlobalExceptionHandler` → 全局异常处理

### 前端架构

- **路由守卫**：未登录只能访问 `/login` 和 `/help`；管理员需 `status >= 2` 才能进入 `/admin`
- **状态持久化**：Pinia store 中 `token` 和 `userInfo` 通过 `persist` 选项存入 localStorage
- **模块启用控制**：`userModelStatus` 追踪每个用户启用的功能模块 (health/finance/knowledge/memo)，前端据此显示/隐藏功能入口
- **Axios 拦截器**：请求自动附加 `Authorization: Bearer <token>`；响应中 `401` 自动清除会话跳转登录页

---

## 代码规范

### 前端

| 规则 | 值 |
|---|---|
| 分号 | 不使用 (`semi: false`) |
| 引号 | 单引号 (`singleQuote: true`) |
| 行宽 | 100 字符 (`printWidth: 100`) |
| 缩进 | 2 空格 |
| 换行符 | LF |
| Linter | oxlint (correctness: error) + ESLint |
| Formatter | Prettier |
| 组件风格 | Vue 3 Composition API (`<script setup>`) |
| Store 风格 | Pinia Composition API (`defineStore` with setup function) |

### 后端

- 使用 **Lombok** (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) 减少 POJO 样板代码
- MyBatis XML 映射文件与 Mapper 接口同包放置 (`classpath:com/createpen/fifteenhundun/module/*/mapper/*.xml`)
- 下划线转驼峰映射已开启 (`map-underscore-to-camel-case: true`)
- 自定义注解驱动横切逻辑 (`@AutoFillTime`, `@AutoLog`)
- 异常体系：业务异常继承自各自语义化类 (如 `LoginException`, `UserNotFoundException`)，由 `GlobalExceptionHandler` 统一捕获

---

## API 代理

前端开发服务器 (Vite, 端口 5577) 通过以下代理配置转发 API 请求：

```
/api/*  →  http://localhost:5757/*
```

`vite.config.js` 中配置了 `rewrite: (path) => path.replace(/^\/api/, '')`，即前端请求 `/api/user/login` 实际转发到 `http://localhost:5757/user/login`。

Axios 实例 `baseURL` 设为 `"/api"`，所有 API 调用自动走代理。

---

## 数据库

数据库名：`fifteen_hundun`，共 11 张表：

| 表名 | 说明 | 与 users 关系 |
|---|---|---|
| `users` | 用户表 (核心) | — |
| `user_model_status` | 用户模块启用状态 | 1:1 (UNIQUE user_id) |
| `article` | 文章/帮助文档 | 创建者关联 |
| `article_comment` | 文章评论 | FK → article |
| `article_user_interact` | 用户-文章交互 (点赞/踩) | 逻辑关联 (UK: user_id+article_id) |
| `feedback` | 用户反馈 | FK → users |
| `memo` | 旧备忘录表 | 逻辑关联 user_id |
| `user_memo_common` | 普通备忘 | FK → users |
| `user_memo_important` | 重要备忘 | FK → users |
| `user_food_health` | 每日饮食健康 | 逻辑关联 (UK: user_id+date) |
| `user_food_finance` | 每日餐饮花费 | 逻辑关联 (UK: user_id+date) |
| `user_payment_finance` | 支付/消费记录 | FK → users |
| `operation_log` | 操作审计日志 | 逻辑关联 operate_user_id |

初始化脚本：`file/hundun.sql`

---

## 测试

当前项目 **无任何测试文件**。后端 `src/test/` 目录不存在，前端亦无测试框架配置。后端已声明 `spring-boot-starter-test` 和 `mybatis-spring-boot-starter-test` 依赖但未使用。

---

## Docker 部署

提供两种部署策略：

### 策略 A：多阶段构建 (`docker-compose.yml`)

- 阶段 1：`maven:3.9-eclipse-temurin-17` 编译打包
- 阶段 2：`eclipse-temurin:17-jre-alpine` 运行 (非 root 用户)
- JVM 参数：`-Xms512m -Xmx1024m -XX:+UseG1GC`
- 健康检查：`/actuator/health` (30s 间隔)
- 需 `.env` 文件提供阿里云 OSS 密钥

### 策略 B：预编译 (`docker-compose.prebuilt.yml`)

- 需提前 `mvn package` 生成 JAR
- MySQL 初始化直接挂载 `hundun.sql`
- 强制 `SPRING_PROFILES_ACTIVE: default` (跳过 dev profile)

Nginx 反向代理配置 (`nginx.conf`)：HTTP:80 → upstream :8080，支持 WebSocket 升级，HTTPS 模板已注释备选。

---

## 安全注意事项

- `application-dev.yml` 中包含 **硬编码的阿里云 OSS AccessKey**，生产部署必须通过环境变量或 `.env` 文件注入，并轮换已泄露的密钥
- 密码使用 BCrypt 哈希存储
- JWT Token 存储于前端 localStorage + Redis 缓存
- 导航守卫在前端实现访问控制，后端 `LoginFilter` 做接口级鉴权
