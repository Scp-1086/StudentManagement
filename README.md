## 项目概述

学生成绩管理系统，前后端分离架构。后端为 Spring Boot REST API，前端为 Java Swing 桌面客户端，通过 HTTP 调用后端接口。管理学生、课程、成绩三类数据，并提供成绩统计功能。

## 技术栈

- Java 8 + Spring Boot 2.7.0
- MyBatis-Plus 3.5.1（注解式 Mapper，无 XML 映射文件）
- Lombok（实体类使用 @Data 等注解）
- MySQL 数据库
- 前端：Java Swing（通过 HttpURLConnection 调用后端 REST API）
- 构建：Maven（非多模块聚合，backend 和 frontend 各自独立构建）

## 常用命令

```bash
# 构建并运行后端（在 StudentManagement/backend/ 目录下）
cd StudentManagement/backend
mvnw spring-boot:run

# 仅构建后端
cd StudentManagement/backend
mvnw clean package

# 构建前端（在 StudentManagement/frontend/ 目录下）
cd StudentManagement/frontend
mvnw clean package

# 运行前端 GUI（需要后端先启动）
java -jar StudentManagement/frontend/target/frontend-1.0-SNAPSHOT.jar
```

## 目录结构

实际项目代码在 `StudentManagement/` 子目录中（注意有两层同名目录），包含：

- `backend/` — Spring Boot 后端
- `frontend/` — Swing 桌面客户端
- `Doc/student_management.sql` — 数据库初始化脚本（建库建表+示例数据）

## 架构

### 后端（backend）

包路径：`com.ndky.studentmanagement`

标准三层架构：

- **Controller** (`controller/`) — REST 控制器，路由前缀 `/api/students`、`/api/courses`、`/api/scores`
- **Service** (`service/` 接口 + `service/impl/` 实现) — 业务逻辑，继承 MyBatis-Plus 的 `IService`/`ServiceImpl`
- **Mapper** (`mapper/`) — 仅接口继承 `BaseMapper`，无自定义 SQL，全部使用 MyBatis-Plus 内置 CRUD
- **Entity** (`entity/`) — 使用 Lombok @Data + MyBatis-Plus 注解
- **Config** (`config/CorsConfig.java`) — CORS 跨域配置
- **Exception** (`exception/`) — 全局异常处理（GlobalExceptionHandler + 自定义异常类）

### 前端（frontend）

包路径：`com.ndky.studentmanagement.gui` + `com.ndky`

- `StudentManagementGUI.java` — 应用入口，JTabbedPane 四个标签页
- `gui/StudentPanel.java` — 学生管理面板（增删改查+搜索）
- `gui/CoursePanel.java` — 课程管理面板
- `gui/ScorePanel.java` — 成绩管理面板
- `gui/StatisticsPanel.java` — 统计查询面板（按学生/课程统计成绩）
- `gui/MainFrame.java` — 备用入口（仅三个标签页，无统计）

每个 Panel 内部通过 `HttpURLConnection` 直接调用 `http://localhost:8080/api/...`，硬编码了后端地址。

## 数据模型

三张表，对应 `Doc/student_management.sql`：

| 表名      | 主键                     | 说明                                      |
| --------- | ------------------------ | ----------------------------------------- |
| `student` | `sno`（学号，VARCHAR）   | 学生信息：sname、ssex、sage、sdept        |
| `course`  | `cno`（课程号，VARCHAR） | 课程信息：cname、ccredit、cdept           |
| `sc`      | `(sno, cno)` 联合主键    | 成绩表：grade，外键关联 student 和 course |

注意：Score 实体映射到 `sc` 表，`@TableId` 只标注了 sno，联合主键在 MyBatis-Plus 中需特殊处理。

## REST API

| 路径                                    | 方法           | 功能              |
| --------------------------------------- | -------------- | ----------------- |
| `/api/students`                         | GET            | 全部学生          |
| `/api/students/{sno}`                   | GET/PUT/DELETE | 单个学生 CRUD     |
| `/api/students/search?sno=&name=&dept=` | GET            | 条件搜索          |
| `/api/students/page?pageNum=&pageSize=` | GET            | 分页查询          |
| `/api/courses`                          | GET            | 全部课程          |
| `/api/courses/{cno}`                    | GET/PUT/DELETE | 单个课程 CRUD     |
| `/api/courses/search?cno=&cname=`       | GET            | 条件搜索          |
| `/api/scores`                           | GET/POST       | 全部成绩/添加成绩 |
| `/api/scores/{sno}/{cno}`               | PUT/DELETE     | 更新/删除成绩     |
| `/api/scores/student/{sno}`             | GET            | 学生成绩          |
| `/api/scores/course/{cno}`              | GET            | 课程成绩          |
| `/api/scores/statistics/student/{sno}`  | GET            | 学生成绩统计      |
| `/api/scores/statistics/course/{cno}`   | GET            | 课程成绩统计      |
| `/test`                                 | GET            | 健康检查          |

## 数据库配置

`backend/src/main/resources/application.properties` 中配置（同时存在 application.yml，properties 优先）：

- 数据库：`student_management`（注意 yml 中写的是 `student`，以 properties 为准）
- 默认用户名/密码：`root` / `password`
- 端口：8080

## 注意事项

- 前端 Panel 类中的 HTTP 请求方法（sendGetRequest/sendPostRequest 等）在每个 Panel 中重复实现，没有提取公共工具类
- Score 实体的联合主键 (sno, cno) 只有 sno 标注了 @TableId，MyBatis-Plus 的 updateById/removeById 对联合主键支持有限
- 前端硬编码了 `http://localhost:8080` 作为后端地址，无法配置
- application.properties 和 application.yml 同时存在，properties 的数据库名是 `student_management`，yml 是 `student`，运行时以 properties 为准
