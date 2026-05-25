# 一五馄饨

一个面向个人的 **生活记录** 平台——用数据记录日常，让生活有迹可循。用户可按需启用四大记录模块：**健康**（饮食记录）、**财务**（消费记录）、**知识**（文章收藏）、**备忘**（待办与重要事项），每个模块都可以独立开启或关闭，只记录自己关心的。

平台同时提供管理员体系与邮件通知能力，用于用户管理、反馈处理和备忘提醒。用户角色分三级：普通用户(status=1)、管理员(status=2)、超级管理员(status=3)。

## 功能概览

### 核心模块

| 模块 | 功能 |
|---|---|
| **健康** | 每日饮食记录（早/午/晚/其他）、数据查询 |
| **财务** | 每日餐饮花费记录、支付/消费记录、财务数据聚合查询 |
| **知识** | 文章/帮助文档发布、点赞/踩（Redis Lua 原子操作）、用户交互记录 |
| **备忘** | 普通备忘（带时间范围+邮件通知）、重要备忘，完整 CRUD |

> 每个用户可独立启用/禁用以上 4 个模块，只开启自己需要的。

### 附属功能

| 功能 | 说明 |
|---|---|
| **用户** | 注册/登录（用户名、邮箱、手机号、验证码 5 种方式）、头像上传、信息管理、密码修改 |
| **管理员** | 用户管理（查看/禁用/删除）、反馈处理、邀请管理员（邀请码）、操作日志审计 |
| **反馈** | 用户提交反馈、管理员处理、邮件通知结果 |

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 3.5.8 | 主框架 |
| Java | 17 | 运行时 |
| MyBatis + MyBatis-Plus | 3.5.5 | ORM |
| PageHelper | 1.4.7 | 分页 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存 + 会话 + 原子操作 (Lua 脚本) |
| JWT | 0.11.5 | 认证 |
| BCrypt | 0.4 | 密码哈希 |
| 阿里云 OSS | 3.17.1 | 文件存储 |
| JavaMail | 1.6.2 | SMTP 邮件 (验证码/通知) |
| RabbitMQ | — | 消息队列 (Spring AMQP) |
| Spring AOP | — | 自动填充时间戳、操作日志 |
| Spring Scheduling | — | 定时任务 (Redis→MySQL 同步) |
| Maven | 3.9.11 | 构建 |

### 前端

| 技术 | 版本 | 用途 |
|---|---|---|
| Vue | 3.5 | 框架 (Composition API) |
| Vite | 7.3 | 构建工具 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 5.0 | 路由 |
| Element Plus | 2.13 | UI 组件库 |
| Axios | 1.13 | HTTP 客户端 |
| Less | 4.6 | CSS 预处理 |
| marked | 18.0 | Markdown 渲染 |
| pnpm | — | 包管理器 |

> Node.js 要求：`^20.19.0 \|\| >=22.12.0`

## 项目结构

```
15-hundun/
├── back-end/
│   ├── fifteen-hundun/                      # Spring Boot 后端
│   │   └── src/main/java/com/createpen/fifteenhundun/
│   │       ├── FifteenHundunApplication.java       # 主入口
│   │       ├── common/                      # 公共组件
│   │       │   ├── anno/                    #   @AutoFillTime, @AutoLog
│   │       │   ├── config/                  #   CORS, Redis, WebSocket, Async
│   │       │   ├── constant/                #   Redis key, 状态码, 类型码
│   │       │   ├── context/                 #   BaseContext (ThreadLocal)
│   │       │   ├── exception/               #   自定义异常 + 全局处理
│   │       │   ├── response/                #   Result<T> 统一响应
│   │       │   └── util/                    #   JWT, OSS, 密码, 验证码, 邮件
│   │       ├── framework/                   # 框架层
│   │       │   ├── aspect/                  #   AOP 切面
│   │       │   ├── interception/            #   JWT LoginFilter, SQL 拦截器
│   │       │   └── task/                    #   定时任务, 启动运行器
│   │       └── module/                      # 业务模块
│   │           ├── article/                 #   文章 (Redis 缓存 + Lua 脚本)
│   │           ├── food/                    #   饮食 (健康 + 财务)
│   │           ├── memo/                    #   备忘 (普通 + 重要)
│   │           ├── payment/                 #   支付/财务
│   │           ├── selecter/                #   数据聚合查询
│   │           └── user/                    #   用户 (登录/管理/反馈/WebSocket)
│   ├── Dockerfile                           # 多阶段构建
│   ├── Dockerfile.prebuilt                  # 预编译 JAR 构建镜像
│   ├── docker-compose.yml                   # 完整构建编排
│   ├── docker-compose.prebuilt.yml          # 预编译编排
│   └── nginx.conf                           # Nginx 反向代理
├── front-end/
│   └── hundun-front/                        # Vue 3 前端
│       └── src/
│           ├── api/                         # API 接口定义
│           ├── assets/                      # 静态资源
│           ├── router/                      # 路由 + 导航守卫
│           ├── stores/                      # Pinia 状态管理
│           ├── utils/                       # 工具函数
│           └── views/                       # 页面组件
│               ├── login/                   #   登录/注册
│               ├── help/                    #   帮助页
│               ├── admin/                   #   管理员后台
│               └── user/                    #   用户主页
└── file/
    └── hundun.sql                           # 数据库初始化脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js ^20.19.0 或 >=22.12.0
- pnpm
- MySQL 8.0+
- Redis 7+

### 后端

1. 创建数据库并导入 `file/hundun.sql`

2. 修改 `back-end/fifteen-hundun/src/main/resources/application-dev.yml` 中的数据库、Redis、OSS 等配置

3. 启动：

```bash
cd back-end/fifteen-hundun
mvn spring-boot:run
```

后端运行在 `http://localhost:5757`

### 前端

```bash
cd front-end/hundun-front
pnpm install
pnpm dev
```

前端运行在 `http://localhost:5577`，API 请求自动代理到后端（`/api/*` → `localhost:5757/*`）

## API 接口

### 认证

| 方法 | 路径 | 描述 |
|---|---|---|
| POST | /user/register | 用户注册 |
| POST | /user/login/username-password | 用户名密码登录 |
| POST | /user/login/email-password | 邮箱密码登录 |
| POST | /user/login/phone-password | 手机密码登录 |
| POST | /user/login/email-code/send | 发送邮箱验证码 |
| POST | /user/login/email-code | 邮箱验证码登录 |
| POST | /user/login/logout | 登出 |

### 用户

| 方法 | 路径 | 描述 |
|---|---|---|
| GET | /user/info | 获取当前用户信息 |
| PUT | /user/avatar | 更新头像 |
| PUT | /user/nickname | 修改昵称 |
| PUT | /user/email | 修改邮箱 |
| PUT | /user/phone | 修改手机号 |
| PUT | /user/password | 修改密码 |
| GET | /user/model-status | 获取模块启用状态 |
| PUT | /user/model-status | 更新模块启用状态 |

### 管理员

| 方法 | 路径 | 描述 |
|---|---|---|
| GET | /admin/invite-code | 获取邀请码 |
| POST | /admin | 添加管理员 |
| PUT | /user/status | 修改用户状态 |
| DELETE | /user/{id} | 删除用户 |
| GET | /logs | 查看操作日志 |

### 反馈

| 方法 | 路径 | 描述 |
|---|---|---|
| GET | /feedback | 获取反馈列表 |
| POST | /feedback | 提交反馈 |
| PUT | /feedback/status | 处理反馈 |
| DELETE | /feedback/handled | 删除已处理反馈 |

### 文章

| 方法 | 路径 | 描述 |
|---|---|---|
| GET | /article | 获取文章列表 |
| POST | /article | 发布文章 |
| POST | /article/good/{id} | 点赞 |
| POST | /article/bad/{id} | 踩 |

### 饮食健康 & 财务

| 方法 | 路径 | 描述 |
|---|---|---|
| POST | /food/health | 记录饮食健康 |
| PUT | /food/health | 更新饮食健康 |
| POST | /food/finance | 记录餐饮花费 |
| PUT | /food/finance | 更新餐饮花费 |
| DELETE | /food/finance/{id} | 删除花费记录 |

### 备忘

| 方法 | 路径 | 描述 |
|---|---|---|
| GET/POST/PUT/DELETE | /memo/common/* | 普通备忘 CRUD |
| GET/POST/PUT/DELETE | /memo/important/* | 重要备忘 CRUD |

## 数据库

数据库名 `fifteen_hundun`，共 11 张表：

| 表名 | 说明 |
|---|---|
| `users` | 用户表 (核心) |
| `user_model_status` | 用户模块启用状态 (1:1) |
| `article` | 文章/帮助文档 |
| `article_comment` | 文章评论 |
| `article_user_interact` | 用户-文章交互 (点赞/踩) |
| `feedback` | 用户反馈 |
| `memo` | 旧备忘录表 |
| `user_memo_common` | 普通备忘 |
| `user_memo_important` | 重要备忘 |
| `user_food_health` | 每日饮食健康 |
| `user_food_finance` | 每日餐饮花费 |
| `user_payment_finance` | 支付/消费记录 |
| `operation_log` | 操作审计日志 |

初始化脚本：`file/hundun.sql`

## Docker 部署

### 方式一：完整构建

包含 Maven 编译阶段，自动构建 JAR：

```bash
cd back-end
# 创建 .env 文件填写阿里云 OSS 密钥
docker-compose up -d
```

### 方式二：预编译

需先在本地构建 JAR，然后只运行运行时镜像：

```bash
cd back-end/fifteen-hundun
mvn clean package -DskipTests
cd ..
docker-compose -f docker-compose.prebuilt.yml up -d
```

Docker 服务：MySQL 8.0 (:3306) + Redis 7 (:6379) + App (:8080)

Nginx 反向代理配置见 `back-end/nginx.conf`，支持 WebSocket 升级，HTTPS 模板已注释备选。

## 配置说明

### Redis

系统使用 Redis 进行缓存和会话管理：

- 用户信息、Token 缓存
- 反馈列表缓存
- 文章点赞/踩计数 (Lua 原子脚本)
- 饮食健康/财务数据缓存
- 用户模块状态缓存

定时任务每日凌晨 2:00 将 Redis 文章交互数据同步到 MySQL。

### 邮件

在 `application-dev.yml` 中配置 SMTP 参数：

- 邮箱地址
- 授权码
- SMTP 主机

### 阿里云 OSS

文件/图片上传使用阿里云 OSS，需在配置文件或 `.env` 中提供 AccessKey。

## 许可证

本项目仅供学习交流使用。
