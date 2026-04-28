# dbstream-driver

基于 JDBC 代理驱动的 Change Data Capture（CDC）组件，通过拦截 SQL 操作捕获数据库变更并发布结构化事件。零运行时依赖。

## Maven 坐标

- groupId: com.codingapi.dbstream
- artifactId: dbstream-driver
- version: 1.0.17
- packaging: jar

## 关联关系

> 以下关系由 `mvn dependency:tree` 指令结果生成，非人工推断。

### 我被哪些模块依赖

本项目为单模块工程，无 reactor 内部依赖方。

### 我依赖哪些模块

无 reactor 内部依赖。

### 主要外部依赖

- `org.projectlombok:lombok`（optional，编译期注解处理）
- 无其他运行期依赖

## 项目结构

```
com.codingapi.dbstream/
├── DBStreamContext.java                 # 对外统一入口（单例）
├── driver/
│   └── DBStreamProxyDriver.java         # java.sql.Driver 实现，SPI 注册
├── proxy/
│   ├── ConnectionProxy.java             # 代理 Connection，拦截 commit/rollback/close
│   ├── PreparedStatementProxy.java      # 代理 PreparedStatement，捕获参数与执行
│   ├── StatementProxy.java              # 代理 Statement
│   └── CallableStatementProxy.java      # 代理 CallableStatement
├── parser/
│   ├── SQLParser.java                   # SQL 解析接口（提取表名）
│   ├── InsertSQLParser.java             # INSERT SQL 正则解析
│   ├── UpdateSQLParser.java             # UPDATE SQL 正则解析
│   ├── DeleteSQLParser.java             # DELETE SQL 正则解析
│   ├── DBEventParser.java               # 事件解析接口（prepare + loadEvents）
│   ├── InsertDBEventParser.java         # INSERT 事件解析
│   ├── UpdateDBEventParser.java         # UPDATE 事件解析（查 pre-image/post-image）
│   └── DeleteDBEventParser.java         # DELETE 事件解析（查 pre-image）
├── event/
│   ├── EventType.java                   # 枚举：INSERT / UPDATE / DELETE
│   ├── DBEvent.java                     # 数据变更事件（表名、类型、数据、主键、事务标识）
│   ├── DBEventPusher.java               # 事件推送接口
│   ├── DefaultDBEventPusher.java        # 默认推送实现（stdout）
│   ├── DBEventContext.java              # 推送者注册与事件分发
│   └── TransactionEventPools.java       # ThreadLocal 事务级事件池
├── listener/
│   ├── SQLExecuteListener.java          # SQL 执行监听接口（order/before/after）
│   ├── SQLRunningContext.java           # 监听器注册中心，编排 before/after 回调
│   ├── SQLRunningState.java             # 单次执行上下文（SQL、参数、元数据）
│   ├── SQLRunningParam.java             # 参数追踪（index/key 两种模式）
│   └── dbevent/
│       ├── DBEventListener.java         # 抽象基类，串联 SQL 解析→事件生成→事务池
│       ├── InsertEventListener.java     # INSERT 监听器（order=100）
│       ├── UpdateEventListener.java     # UPDATE 监听器（order=100）
│       ├── DeleteEventListener.java     # DELETE 监听器（order=100）
│       └── DBEventCacheContext.java     # ThreadLocal 缓存 DBEventParser（支持批量）
├── scanner/
│   ├── DBScanner.java                   # 数据库元数据扫描（DatabaseMetaData）
│   ├── DBMetaData.java                  # 单数据源全部表元数据
│   ├── DBMetaContext.java               # 元数据缓存（单例，key=jdbcKey）
│   ├── DbTable.java                     # 表元数据（列、主键）
│   ├── DbColumn.java                    # 列元数据（类型、是否主键）
│   └── JavaDBTypeConvertor.java         # JDBC 类型→Java 类型映射
├── supporter/
│   ├── DBEventSupporter.java            # 表级事件支持判断接口
│   └── DefaultDBEventSupporter.java     # 默认实现（始终支持）
├── query/
│   └── JdbcQuery.java                   # 通过真实 Connection 执行 SELECT 的工具
├── serializable/
│   └── DBTableSerializableHelper.java   # DbTable Java 序列化至 .dbstream/ 缓存目录
└── utils/
    ├── SQLUtils.java                    # SQL 分类判断（isInsert/isUpdate/isDelete）
    ├── JdbcPropertyUtils.java           # jdbcKey 生成：sha256(jdbcUrl#schema)
    ├── SHA256Utils.java                 # SHA-256 哈希
    ├── VersionUtils.java                # 从 MANIFEST.MF 读取版本号
    ├── FileReaderUtils.java             # 读取 .key 文件（手动主键配置）
    └── ResultSetUtils.java              # 结果集判断工具
```

## 核心功能

### 1. JDBC 代理驱动

通过 Java SPI（`META-INF/services/java.sql.Driver`）注册 `DBStreamProxyDriver`。`connect()` 时自动发现真实驱动、扫描数据库元数据，返回 `ConnectionProxy`。所有 Statement 创建均被代理。

### 2. SQL 解析与事件生成

基于正则的 SQL 解析器（`InsertSQLParser`/`UpdateSQLParser`/`DeleteSQLParser`）提取表名、列、WHERE 子句。`DBEventParser` 在 `before()` 阶段执行 prepare（UPDATE/DELETE 查询 pre-image），在 `after()` 阶段组装 `DBEvent` 对象。

### 3. 事务级事件管理

`TransactionEventPools` 使用 ThreadLocal 累积事件。auto-commit 模式下立即推送；手动事务下在 `commit()` 时批量推送，`rollback()` 时丢弃。

### 4. 数据库元数据扫描与缓存

`DBScanner` 通过 `java.sql.DatabaseMetaData` 扫描表结构，序列化到 `.dbstream/<jdbcKey>/` 目录缓存。支持 `.key` 文件手动指定主键。

## 对外 API

### DBStreamContext（`com.codingapi.dbstream.DBStreamContext`）

全局单例，所有公共能力的统一入口。

| 方法 | 说明 |
|------|------|
| `getInstance()` | 获取单例 |
| `addListener(SQLExecuteListener)` | 注册 SQL 执行监听器 |
| `cleanCustomListeners()` | 清除自定义监听器（保留系统监听器） |
| `addEventPusher(DBEventPusher)` | 注册事件推送者 |
| `cleanEventPushers()` | 清除所有推送者 |
| `setDbEventSupporter(DBEventSupporter)` | 设置表级事件支持过滤器 |
| `getMetaData(jdbcKey)` | 获取指定数据源元数据 |
| `loadDbKeys()` | 列出所有已注册数据源 key |
| `clearAll()` / `clear(jdbcKey)` | 清除元数据缓存 |

### SQLExecuteListener（`com.codingapi.dbstream.listener.SQLExecuteListener`）

SQL 执行监听接口，按 `order()` 排序执行。

| 方法 | 说明 |
|------|------|
| `order()` | 执行顺序，越小越靠前 |
| `before(SQLRunningState)` | SQL 执行前回调 |
| `after(SQLRunningState, Object)` | SQL 执行后回调 |

### DBEventPusher（`com.codingapi.dbstream.event.DBEventPusher`）

事件推送接口，事务提交时调用。

| 方法 | 说明 |
|------|------|
| `push(JdbcQuery, List<DBEvent>)` | 推送事件列表 |

### DBEventSupporter（`com.codingapi.dbstream.supporter.DBEventSupporter`）

表级事件过滤接口。

| 方法 | 说明 |
|------|------|
| `support(Properties, DbTable)` | 判断是否对该表启用事件捕获 |

## 模块规范

- Java 8 兼容，不使用高于 Java 8 的 API
- 单例模式统一使用 `private static final` 饿汉初始化
- SQL 解析基于正则，不引入外部解析库
- 并发控制：ThreadLocal 隔离事务状态，ConcurrentHashMap 缓存驱动/元数据，CopyOnWriteArrayList 管理监听器
- Lombok 仅用于编译期，标记为 optional，不传递给下游

## 构建指令

```bash
./mvnw compile
./mvnw test
./mvnw package
./mvnw clean deploy -P ossrh
```
