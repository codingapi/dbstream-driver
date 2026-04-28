---
module: dbstream-driver
name: DBStreamContext
description: DBStream 的全局统一入口，用于注册 SQL 执行监听器、事件推送者、表级过滤器，以及管理数据库元数据缓存。
---

# DBStreamContext

- **来源**: 自有
- **所属 module**: dbstream-driver
- **Maven 坐标**: com.codingapi.dbstream:dbstream-driver:1.0.18

## 何时使用

当需要在应用中集成 DBStream 的 CDC 能力时，通过 `DBStreamContext` 单例完成所有配置：注册自定义的 SQL 执行监听器、设置数据变更事件推送目标、过滤需要捕获事件的表，以及查询和管理数据库元数据。

## 如何引用

### Maven 坐标

```xml
<dependency>
    <groupId>com.codingapi.dbstream</groupId>
    <artifactId>dbstream-driver</artifactId>
    <version>1.0.18</version>
</dependency>
```

## API 说明

### 核心类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `DBStreamContext` | `com.codingapi.dbstream` | 全局单例入口，聚合所有公共 API |
| `SQLExecuteListener` | `com.codingapi.dbstream.listener` | SQL 执行监听接口（before/after） |
| `SQLRunningContext` | `com.codingapi.dbstream.listener` | 监听器注册中心，按 order 编排回调 |
| `SQLRunningState` | `com.codingapi.dbstream.listener` | 单次 SQL 执行上下文（SQL、参数、元数据） |
| `DBEventPusher` | `com.codingapi.dbstream.event` | 数据变更事件推送接口 |
| `DBEventContext` | `com.codingapi.dbstream.event` | 推送者注册与事件分发 |
| `DBEvent` | `com.codingapi.dbstream.event` | 数据变更事件（表名、类型、数据、主键、事务标识） |
| `DBEventSupporter` | `com.codingapi.dbstream.supporter` | 表级事件过滤接口 |
| `DBMetaData` | `com.codingapi.dbstream.scanner` | 单数据源全部表元数据 |
| `DBMetaContext` | `com.codingapi.dbstream.scanner` | 元数据缓存单例（key=jdbcKey） |

### 关键方法

#### 监听器管理

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `addListener(SQLExecuteListener)` | SQL 执行监听器 | void | 注册自定义监听器，按 `order()` 排序执行 |
| `cleanCustomListeners()` | 无 | void | 清除所有自定义监听器，保留三个系统监听器（Insert/Update/Delete） |

#### 事件推送管理

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `addEventPusher(DBEventPusher)` | 事件推送实现 | void | 注册推送者，事务提交时回调 `push()` |
| `cleanEventPushers()` | 无 | void | 清除所有已注册的推送者 |

#### 事件过滤

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `setDbEventSupporter(DBEventSupporter)` | 自定义过滤器 | void | 设置表级事件支持判断，默认全部支持 |
| `support(Properties, DbTable)` | 连接属性、表元数据 | boolean | 判断是否对该表启用事件捕获（要求表有列和主键） |

#### 元数据查询

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `getMetaData(String)` | jdbcKey | DBMetaData | 获取指定数据源的元数据 |
| `loadDbKeys()` | 无 | List\<String\> | 列出所有已注册数据源的 key |
| `metaDataList()` | 无 | List\<DBMetaData\> | 返回全部数据源元数据列表 |
| `clearAll()` | 无 | void | 清除所有元数据缓存，下次访问时重新扫描 |
| `clear(String)` | jdbcKey | void | 清除指定数据源的元数据缓存 |

#### 元数据刷新

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `refreshTable(Connection, String, String)` | 数据库连接、jdbcKey、表名 | void | 刷新指定表的元数据。适用于运行时动态创建或修改表结构后手动触发更新。会清除该表的序列化缓存并重新扫描 |
| `refreshAll(Connection, String)` | 数据库连接、jdbcKey | void | 全量刷新指定数据源的元数据。重新扫描所有表结构并更新缓存 |

### 配置项

| 配置Key | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| `jdbc.url` | String（Properties 内） | 无 | 数据库连接 URL，自动写入 Properties |
| `jdbc.key` | String（Properties 内） | sha256(jdbcUrl#schema) | 数据源唯一标识，自动生成 |

## 使用示例

### 基础用法：注册事件推送者

```java
DBStreamContext ctx = DBStreamContext.getInstance();

// 注册事件推送者（事务提交时收到变更事件）
ctx.addEventPusher((jdbcQuery, events) -> {
    for (DBEvent event : events) {
        System.out.println(event.getType() + " on " + event.getTableName());
        System.out.println("data: " + event.getData());
        System.out.println("primaryKeys: " + event.getPrimaryKeys());
    }
});

// 之后正常使用 JDBC/JPA 操作数据库即可自动捕获事件
```

### 注册 SQL 执行监听器

```java
ctx.addListener(new SQLExecuteListener() {
    @Override
    public int order() {
        return 0; // 越小越先执行
    }

    @Override
    public void before(SQLRunningState state) {
        System.out.println("执行 SQL: " + state.getSql());
        System.out.println("参数: " + state.getListParams());
    }

    @Override
    public void after(SQLRunningState state, Object result) {
        System.out.println("执行耗时: " + state.getExecuteTimestamp());
    }
});
```

### 过滤需要捕获的表

```java
ctx.setDbEventSupporter((info, dbTable) -> {
    // 仅捕获 t_order 开头的表
    return dbTable.getName().startsWith("t_order");
});
```

### 清理与重置

```java
// 清除自定义监听器（保留系统级 Insert/Update/Delete 监听器）
ctx.cleanCustomListeners();

// 清除所有事件推送者
ctx.cleanEventPushers();

// 清除元数据缓存（下次连接时自动重新扫描）
ctx.clearAll();
```

### 动态刷新元数据

```java
// 运行时创建了新表或修改了表结构后，手动刷新元数据
try (Connection conn = dataSource.getConnection()) {
    String jdbcKey = ctx.loadDbKeys().get(0);

    // 刷新单张表
    ctx.refreshTable(conn, jdbcKey, "t_new_table");

    // 或全量刷新所有表
    ctx.refreshAll(conn, jdbcKey);
}
```

## 注意事项

- **单例模式**: `DBStreamContext` 使用饿汉单例（`private static final`），全局唯一实例通过 `getInstance()` 获取
- **线程安全**: 监听器列表使用 `CopyOnWriteArrayList`，元数据缓存使用 `ConcurrentHashMap`，支持并发注册和查询；但 `setDbEventSupporter()` 不是线程安全的，建议在应用启动阶段一次性设置
- **系统监听器不可移除**: `cleanCustomListeners()` 仅移除自定义监听器，InsertEventListener / UpdateEventListener / DeleteEventListener 三个系统监听器始终保留
- **默认推送行为**: 未注册任何 `DBEventPusher` 时，事件输出到 stdout（`DefaultDBEventPusher`）
- **元数据自动加载**: 清除元数据缓存后，下次数据库连接时会自动重新扫描表结构，无需手动触发
- **事件过滤前置条件**: 即使 `DBEventSupporter` 返回 true，表仍须具备列定义和主键才会被纳入事件捕获
- **元数据刷新**: `refreshTable()` 和 `refreshAll()` 接受标准 `java.sql.Connection`（不需要是 ConnectionProxy），适用于运行时动态 DDL 场景。`refreshTable()` 会先清除该表的序列化缓存文件再重新扫描

## 变更说明

- **2026-04-28**: 同步最新代码，新增 `refreshTable(Connection, String, String)` 和 `refreshAll(Connection, String)` 元数据刷新 API 文档及使用示例；Maven 坐标版本号更新为 1.0.18
