# 智能惠农信贷系统 —— 后端服务

Spring Boot 3.2 + Spring Security + JWT + Spring Data JPA + AOP 操作日志，为 `smart-agri-loan-vue` 前端提供接口。

> 代码分层、各文件职责、改动该往哪加 → 见 [`docs/项目结构与代码说明.md`](docs/项目结构与代码说明.md)

## 一、技术栈

| 分层 | 技术 |
| --- | --- |
| 框架 | Spring Boot 3.2.5、Java 17 |
| 安全 | Spring Security 6 + JWT（jjwt 0.12.5）+ BCrypt |
| 持久层 | Spring Data JPA / Hibernate |
| 数据库 | 默认 MySQL 8（**启动自动建库**，无需手工建库）；`profile=h2` 可切内存库演示 |
| 审计 | Spring AOP 自定义 `@OpLog` 注解 + 切面入库 |
| 校验 | Jakarta Validation |
| 构建 | Maven |

## 二、快速启动

### 使用 MySQL（默认，一键启动即建库）

```bash
# 只要 MySQL 服务已启动，直接跑即可：
mvn spring-boot:run
# 或 java -jar target/smart-agri-loan-server-1.0.0.jar
```

启动时自动完成三件事，**库已存在则原样跳过，不会覆盖或重建**：

| 顺序 | 由谁负责 | 做什么 |
| --- | --- | --- |
| ① | `DatabaseBootstrap` | 库不存在时 `CREATE DATABASE smart_agri_loan`（utf8mb4） |
| ② | JPA `ddl-auto=update` | 建表 / 补字段（7 张表） |
| ③ | `DataInitializer` | 仅在表为空时灌入演示数据 |

- 连接信息：`src/main/resources/application-mysql.yml`（默认 `127.0.0.1:3306/smart_agri_loan`，账号密码 `root/root`，请改成你本机的）
- 关闭自动建库：`app.datasource.auto-create-database: false`
- 手工建库（可选，等价于 ①②，见 `db/schema.sql`）：`mysql -uroot -p < db/schema.sql`

**"原来的数据"从哪来**：不是写死在 SQL 里的，而是 `DataInitializer` 用 Java 代码构造后经 JPA 写库，
所以 H2 / MySQL 两套环境的数据完全一致。首次启动会灌入 6 款贷款产品、6 条惠农资讯、
3 个账号（`farmer` / `coop` / `auditor`）、12 张覆盖全生命周期的申请单与 8 条信用积分日志；
之后就交给管理员通过 `/api/admin/**` 与新增/修改接口维护，代码不再参与。

### 使用 H2 内存库（免安装演示）

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

- 服务地址：`http://localhost:8080`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL `jdbc:h2:mem:agriloan`，用户 `sa`，密码空）

### 前端联调启动

前端是原生 HTML + Vue 3（CDN 引入，无需构建打包），起一个静态服务即可：

```bash
cd smart-agri-loan-vue
python -m http.server 8090          # 然后访问 http://localhost:8090/index.html
```

接口根地址在 `js/api.js` 顶部：`var API_BASE = global.API_BASE || 'http://localhost:8080';`
后端若换端口，在页面里先执行 `window.API_BASE = 'http://host:port'` 即可覆盖，无需改代码。

> 注意：H2 是**内存库**（仅 `profile=h2` 时），后端每次重启都会重建种子数据。
> 演示或跑自动化用例前重启一次后端，信用积分与申请单即回到初始状态；
> 反之，连续多次运行「申请 → 放款 → 结清」闭环会让信用积分累积上涨。
>
> 默认的 MySQL 是**持久库**，数据跨重启保留，种子数据仅在表为空时灌入一次。

## 三、默认账号（密码均为 `123456`）

| 账号 | 角色 | 说明 | 信用等级 |
| --- | --- | --- | --- |
| `farmer` | 农户 | 张大山，可申请贷款、签约、还款 | AA（762 分） |
| `coop` | 农业企业 | 湄潭雲雾茶业专业合作社 | AAA（805 分） |
| `auditor` | 审批人员 | 李慧·风控审批员，可审批、看操作日志 | — |

> 注册接口只允许注册「农户 / 农业企业」，审批人员账号由后台（种子数据或管理员）开通。

## 四、角色与权限矩阵

| 功能 | 农户 | 农业企业 | 审批人员 |
| --- | :-: | :-: | :-: |
| 浏览产品 / 资讯 / 首页统计 | ✅（含游客） | ✅ | ✅ |
| 查看个人信用积分与五维画像 | ✅ | ✅ | ✅ |
| 提交贷款申请 | ✅ | ✅ | ❌ |
| 查看申请列表 | 仅本人 | 仅本人 | 全部 |
| 签约 / 放款 / 结清 / 充值 | 仅本人单子 | 仅本人单子 | ❌ |
| 初审通过 / 拒绝 | ❌ | ❌ | ✅ |
| 用户列表、新增/修改用户、调整信用积分 | ❌ | ❌ | ✅ |
| 产品新增/修改/上下架、资讯新增/修改 | ❌ | ❌ | ✅ |
| AOP 操作日志查询 | ❌ | ❌ | ✅ |

前端根据 `GET /api/auth/me` 返回的 `roleCode` 隐藏无权限菜单与按钮，后端在 `SecurityConfig` 与服务层双重校验。

## 五、接口一览

### 认证 `/api/auth`
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/register` | 注册（农户 / 农业企业） |
| POST | `/login` | 登录，返回 JWT |
| GET | `/me` | 当前用户（含角色、信用等级、权益） |

### 产品 `/api/products`（公开）
`GET /api/products?type=信用贷&sort=rate`、`GET /api/products/hot`、`GET /api/products/stats`、`GET /api/products/{id}`

返回体中的 `canApply` / `applyTip` 由后端按登录人信用等级计算：低信用用户会收到"准入等级不足"的提示，前端据此置灰按钮（自适应界面）。

### 资讯 `/api/news`（公开）
`GET /api/news?category=政策解读`、`GET /api/news/latest`、`GET /api/news/{id}`（浏览量 +1）

### 信用 `/api/credit`
`GET /api/credit/me`：积分、等级、评价、五维评分、变动日志、等级权益、`policy`（额度 / 利率下浮 / 是否极速审批 / 是否需担保）

### 贷款申请 `/api/applications`
| 方法 | 路径 | 角色 |
| --- | --- | --- |
| GET | `?status=待初审` | 本人 / 审批员 |
| GET | `/{id}` | 本人 / 审批员 |
| POST | `/` | 农户、企业 |
| POST | `/{id}/sign` | 申请人本人 |
| POST | `/{id}/loan` | 申请人本人 |
| POST | `/{id}/settle` | 申请人本人 |
| POST | `/{id}/audit` | 审批人员（body: `{"action":"pass|reject","opinion":"..."}`） |

### 账户 / 管理 / 日志
- `POST /api/account/recharge` 充值（body: `{"amount":10000}`）
- `GET /api/users`、`POST /api/users/{id}/credit` 用户管理（审批人员）
- `GET /api/oplog?keyword=&module=&success=&page=1&size=10` AOP 操作日志（审批人员）

### 管理端维护 /api/admin（仅审批人员）
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/products` | 全部产品（含已下架） |
| GET | `/api/admin/news` | 全部资讯（含已下架） |
| POST | `/api/products` | 新增产品，`id` 留空自动生成 P00X |
| PUT | `/api/products/{id}` | 修改产品；`enabled=false` 即下架 |
| POST | `/api/news` | 新增资讯，`id` 留空自动生成 N00X |
| PUT | `/api/news/{id}` | 修改资讯；`enabled=false` 即下架 |
| POST | `/api/users` | 新增账号（可开通审批人员） |
| PUT | `/api/users/{id}` | 修改资料 / 信用档案 / 启用停用 / 改密 |

修改接口为**增量语义**：请求里没带的字段保持原值，要清空文本字段传 `""`。

新增产品的请求示例：

```json
{
  "name": "秋收周转贷",
  "type": "信用贷",
  "iconText": "秋",
  "rate": 3.9,
  "termMonths": 12,
  "terms": ["6", "12"],
  "minAmount": 10000,
  "maxAmount": 200000,
  "repayment": "到期一次性还本付息",
  "guarantee": "纯信用",
  "target": "从事秋粮种植的农户",
  "features": ["线上申请 3 分钟", "随借随还"],
  "hot": true,
  "minCreditLevel": "A"
}
```

## 六、统一响应

```json
{ "code": 200, "message": "success", "data": { } }
```

`code`：200 成功、400 参数/业务错误、401 未登录、403 无权限、500 服务异常。前端 `js/api.js` 统一拦截：401 跳登录，其余弹 toast。

## 七、AOP 操作日志

`@OpLog(module = "贷款申请", action = "提交贷款申请", target = "#request.productId")`

切面对以下操作自动留痕（成功与失败都记录）：注册、登录、提交申请、签约、放款、结清、充值、审批、人工调分，
以及管理端的**产品新增/修改、资讯新增/修改、账号新增/修改**。
记录字段：操作人、角色、模块、动作、目标、方法、入参（密码自动脱敏）、IP、耗时、成功/失败与异常信息。

## 八、目录结构

```
smart-agri-loan-server
├── db/schema.sql                 # MySQL 建库脚本（手工执行用，非必须）
├── docs/项目结构与代码说明.md      # 代码分层与各文件职责
├── pom.xml
└── src/main
    ├── java/com/agriloan
    │   ├── aop/                  # @OpLog 注解 + 操作日志切面
    │   ├── common/               # 统一响应、分页、异常处理
    │   ├── config/               # 启动自动建库 + 种子数据初始化
    │   ├── controller/           # REST 接口
    │   ├── domain/               # 实体与枚举
    │   ├── dto/                  # 出入参对象
    │   ├── repository/           # Spring Data JPA
    │   ├── security/             # JWT、过滤器、SecurityConfig
    │   ├── service/              # 业务与信用等级策略
    │   └── support/              # 实体 -> DTO 转换（含自适应判定）
    └── resources
        ├── application.yml       # 公共配置（默认激活 mysql）
        ├── application-mysql.yml # MySQL 数据源
        └── application-h2.yml    # H2 内存库（profile=h2）
```

## 九、信用等级与自适应策略

等级策略集中在 `CreditPolicyService`，是全站"因人而异"的唯一数据源：

| 等级 | 积分 | 最高纯信用额度 | 利率下浮 | 极速审批 | 需担保 | 纯信用贷准入 |
| --- | --- | --- | --- | --- | --- | --- |
| AAA | 850+ | 50 万 | 30BP | ✅ 1 天 | ❌ | ✅ |
| AA | 750+ | 30 万 | 15BP | ✅ 1.5 天 | ❌ | ✅ |
| A | 650+ | 15 万 | — | ❌ 3 天 | ❌ | ✅ |
| B | 550+ | 5 万 | — | ❌ 5 天 | ✅ | ✅（需增信） |
| C | <550 | 0 | — | ❌ | ✅ | ❌ |

还款结清后信用积分 +5、履约能力 +1；积分跨过阈值时等级自动跃迁，前端页面随之切换文案、权益与准入提示。
