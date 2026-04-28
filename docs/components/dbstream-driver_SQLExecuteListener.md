---
module: dbstream-driver
name: SQLExecuteListener
description: SQL 执行拦截监听接口，允许在 SQL 执行前后插入自定义逻辑，如日志记录、性能监控、审计追踪等。
---

# SQLExecuteListener

- **来源**: 自有
- **所属 module**: dbstream-driver
- **Maven 坐标**: com.codingapi.dbstream:dbstream-driver:1.0.17

## 何时使用

需要在 SQL 执行前后插入自定义处理逻辑时使用，例如：SQL 审计日志、慢查询监控、SQL 改写、性能统计。DBStream 内部的三个系统监听器（Insert/Update/Delete）也基于此接口实现数据变更事件捕获。

## 如何引用

### Maven 坐标

```xml
<dependency>
    <groupId>com.codingapi.dbstream</groupId>
    <artifactId>dbstream-driver</artifactId>
    <version>1.0.17</version>
</dependency>
```

## API 说明

### 核心类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `SQLExecuteListener` | `com.codingapi.dbstream.listener` | SQL 执行监听接口，自定义监听器的契约 |
| `SQLRunningContext` | `com.codingapi.dbstream.listener` | 监听器注册中心（单例），按 order 编排 before/after 回调 |
| `SQLRunningState` | `com.codingapi.dbstream.listener` | 单次 SQL 执行的完整上下文（SQL、参数、元数据、执行耗时） |
| `SQLRunningParam` | `com.codingapi.dbstream.listener` | 参数追踪器，支持 index 和 key 两种参数绑定模式 |
| `DBEventListener` | `com.codingapi.dbstream.listener.dbevent` | 抽象基类，实现 `SQLExecuteListener`，串联 SQL 解析→事件生成→事务池 |
| `InsertEventListener` | `com.codingapi.dbstream.listener.dbevent` | INSERT 系统监听器（order=100） |
| `UpdateEventListener` | `com.codingapi.dbstream.listener.dbevent` | UPDATE 系统监听器（order=100） |
| `DeleteEventListener` | `com.codingapi.dbstream.listener.dbevent` | DELETE 系统监听器（order=100） |
| `JdbcQuery` | `com.codingapi.dbstream.query` | 通过真实 Connection 执行 SELECT 查询的工具，在 `SQLRunningState` 中暴露 |

### 关键方法

#### SQLExecuteListener 接口方法

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `order()` | 无 | int | 执行顺序，值越小越先执行。系统监听器 order=100，自定义监听器建议用更小的值以优先执行 |
| `before(SQLRunningState)` | SQL 执行上下文 | void | SQL 执行前回调。可读取 SQL、参数、元数据；抛出 SQLException 可阻止执行 |
| `after(SQLRunningState, Object)` | SQL 执行上下文 + 执行结果 | void | SQL 执行后回调。result 通常是 int（影响行数）或 ResultSet |

#### SQLRunningState 关键方法

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `getSql()` | String | 当前执行的 SQL 语句 |
| `getListParams()` | List\<Object\> | 按索引排序的参数值列表 |
| `getMetaData()` | DBMetaData | 当前数据源的元数据 |
| `getDbTable(String)` | DbTable | 按表名查找元数据 |
| `getTransactionKey()` | String | 当前事务标识 |
| `getJdbcUrl()` | String | 当前数据库连接 URL |
| `getJdbcKey()` | String | 当前数据源唯一标识 |
| `getDriverProperties()` | Properties | 驱动配置信息 |
| `getExecuteTimestamp()` | long | SQL 执行耗时（毫秒，after 减 before） |
| `getBeginTimestamp()` | long | 执行开始时间戳 |
| `getAfterTimestamp()` | long | 执行结束时间戳 |
| `isJdbcBatchMode()` | boolean | 是否处于 JDBC 批量模式 |
| `query(String)` | List\<Map\<String, Object\>\> | 执行 SELECT 查询（通过真实连接） |
| `query(String, List\<Object\>)` | List\<Map\<String, Object\>\> | 带参数的 SELECT 查询 |
| `getStatementGenerateKeys(DbTable)` | List\<Map\<String, Object\>\> | 获取自增主键生成结果 |
| `getResult()` | Object | SQL 执行结果 |

#### SQLRunningContext 管理方法

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `SQLRunningContext.getInstance()` | SQLRunningContext | 获取单例 |
| `addListener(SQLExecuteListener)` | void | 注册监听器（去重 + 按 order 排序） |
| `cleanCustomListeners()` | void | 移除自定义监听器，保留三个系统监听器 |
| `before(SQLRunningState)` | void | 触发所有监听器的 before 回调 |
| `after(SQLRunningState, Object)` | void | 触发所有监听器的 after 回调 |

### 配置项

本组件无独立配置项。监听器通过 `DBStreamContext.addListener()` 代码注册。

## 使用示例

### 基础用法：SQL 审计日志

```java
DBStreamContext.getInstance().addListener(new SQLExecuteListener() {
    @Override
    public int order() {
        return 0; // 优先于系统监听器（order=100）执行
    }

    @Override
    public void before(SQLRunningState state) {
        System.out.println("执行 SQL: " + state.getSql());
        System.out.println("参数: " + state.getListParams());
    }

    @Override
    public void after(SQLRunningState state, Object result) {
        System.out.println("执行耗时: " + state.getExecuteTimestamp() + "ms");
    }
});
```

### 慢查询监控

```java
DBStreamContext.getInstance().addListener(new SQLExecuteListener() {
    @Override
    public int order() { return 0; }

    @Override
    public void before(SQLRunningState state) {}

    @Override
    public void after(SQLRunningState state, Object result) {
        long cost = state.getExecuteTimestamp();
        if (cost > 1000) {
            log.warn("慢查询 [{}ms]: {} params={}", cost, state.getSql(), state.getListParams());
        }
    }
});
```

### 在 before 中执行辅助查询

```java
@Override
public void before(SQLRunningState state) throws SQLException {
    // 利用 SQLRunningState.query() 通过真实连接查询数据
    String tableName = "t_config";
    DbTable table = state.getDbTable(tableName);
    if (table != null) {
        List<Map<String, Object>> rows = state.query("SELECT * FROM t_config WHERE key = ?", params);
        // 基于查询结果做处理
    }
}
```

## 注意事项

- **执行顺序**: 系统监听器（Insert/Update/Delete）order=100。自定义监听器 order < 100 时在 before 中先于系统监听器执行；order > 100 时则在系统监听器之后
- **系统监听器不可移除**: `cleanCustomListeners()` 仅清除自定义监听器，Insert/Update/Delete 三个系统监听器始终存在
- **before 抛异常阻止执行**: 在 `before()` 中抛出 `SQLException` 会中止当前 SQL 执行链，后续监听器的 before/after 均不会执行
- **批量模式**: 当使用 JDBC batch 操作时，`SQLRunningState.isJdbcBatchMode()` 返回 true，`getBatchSQLRunningStateList()` 返回每条 SQL 的独立上下文
- **线程安全**: `SQLRunningContext` 的监听器列表使用 `CopyOnWriteArrayList`，支持运行时动态注册。但建议在应用启动阶段一次性完成注册
- **query() 走真实连接**: `SQLRunningState.query()` 使用未被代理的真实 `Connection`，不会触发监听器递归调用
- **注册去重**: `SQLRunningContext.addListener()` 会检查监听器是否已存在（基于 equals），重复注册不会生效
