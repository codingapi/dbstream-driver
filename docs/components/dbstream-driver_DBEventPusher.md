---
module: dbstream-driver
name: DBEventPusher
description: 数据变更事件推送接口，在事务提交时接收 DBEvent 列表并推送到外部系统（消息队列、日志、缓存同步等）。
---

# DBEventPusher

- **来源**: 自有
- **所属 module**: dbstream-driver
- **Maven 坐标**: com.codingapi.dbstream:dbstream-driver:1.0.18

## 何时使用

需要将数据库变更事件推送到外部系统时使用，例如：同步数据到 Elasticsearch/Redis、发送变更通知到消息队列、构建审计日志、实现跨服务数据一致性。未注册任何 `DBEventPusher` 时，事件默认输出到 stdout。

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
| `DBEventPusher` | `com.codingapi.dbstream.event` | 事件推送接口，业务方实现此接口 |
| `DefaultDBEventPusher` | `com.codingapi.dbstream.event` | 默认实现，将事件打印到 stdout |
| `DBEventContext` | `com.codingapi.dbstream.event` | 推送者注册中心（单例），管理推送者列表并分发事件 |
| `DBEvent` | `com.codingapi.dbstream.event` | 数据变更事件对象（表名、类型、数据、主键、事务标识） |
| `EventType` | `com.codingapi.dbstream.event` | 事件类型枚举：INSERT / UPDATE / DELETE |
| `TransactionEventPools` | `com.codingapi.dbstream.event` | 事务级事件池（ThreadLocal），管理事件的累积与提交/回滚 |
| `JdbcQuery` | `com.codingapi.dbstream.query` | JDBC 查询工具，在 push 回调中可用来查询补充数据 |

### 关键方法

#### DBEventPusher 接口

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `push(JdbcQuery, List<DBEvent>)` | jdbcQuery 可用于查询补充数据；events 为本次事务产生的变更事件列表 | void | 事务提交时调用。auto-commit 模式下每条 SQL 执行后立即调用；手动事务下在 commit() 时批量调用 |

#### DBEvent 数据访问方法

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `getTableName()` | String | 变更的数据表名称 |
| `getType()` | EventType | 事件类型：INSERT / UPDATE / DELETE |
| `isInsert()` / `isUpdate()` / `isDelete()` | boolean | 便捷类型判断 |
| `getData()` | Map\<String, Object\> | 变更数据（INSERT=新数据，UPDATE=更新后数据，DELETE=删除前数据） |
| `set(String key, Object value)` | void | 向 data Map 中添加键值对 |
| `getPrimaryKeys()` | List\<String\> | 主键列名列表 |
| `addPrimaryKey(String)` | void | 添加主键列名（去重） |
| `hasPrimaryKeys()` | boolean | 是否有主键信息 |
| `getTransactionKey()` | String | 所属事务标识 |
| `getJdbcKey()` | String | 数据源唯一标识 |
| `getJdbcUrl()` | String | 数据库连接 URL |
| `getTimestamp()` | long | 事件创建时间戳 |
| `getPushTimestamp()` | long | 事件推送时间戳 |

#### EventType 枚举值

| 值 | 说明 |
|----|------|
| `INSERT` | 插入事件 |
| `UPDATE` | 更新事件 |
| `DELETE` | 删除事件 |

#### DBEventContext 管理方法

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `DBEventContext.getInstance()` | DBEventContext | 获取单例 |
| `addPusher(DBEventPusher)` | void | 注册推送者 |
| `clean()` | void | 清除所有已注册的推送者 |

#### TransactionEventPools 关键方法

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `isAutoCommit()` | boolean | 当前是否自动提交模式 |
| `setAutoCommit(boolean)` | void | 设置自动提交模式 |
| `addEvents(JdbcQuery, String, List<DBEvent>)` | void | 添加事件，auto-commit 模式下立即推送 |
| `commitEvents(JdbcQuery, String)` | void | 设置事务标识并推送全部累积事件 |
| `rollbackEvents(JdbcQuery, String)` | void | 回滚，丢弃所有累积事件 |
| `clear()` | void | 清除事件池 ThreadLocal |
| `reset()` | void | 同时清除事件池和自动提交模式 ThreadLocal |

### 事件数据说明

| EventType | data 内容 | 说明 |
|-----------|----------|------|
| INSERT | 新插入行的列值 | 主键通过 `getStatementGenerateKeys()` 或 SQL 解析获取 |
| UPDATE | 更新后的行数据（post-image） | UPDATE 事件在 before 阶段查询 pre-image，after 阶段查询 post-image |
| DELETE | 删除前的行数据（pre-image） | 在 before 阶段通过 WHERE 条件查询当前数据 |

### 配置项

本组件无独立配置项。推送者通过 `DBStreamContext.addEventPusher()` 代码注册。

## 使用示例

### 基础用法：注册事件推送者

```java
DBStreamContext ctx = DBStreamContext.getInstance();

ctx.addEventPusher((jdbcQuery, events) -> {
    for (DBEvent event : events) {
        String action = event.isInsert() ? "新增" :
                        event.isUpdate() ? "修改" : "删除";
        System.out.println(action + " 表:" + event.getTableName());
        System.out.println("主键:" + event.getPrimaryKeys());
        System.out.println("数据:" + event.getData());
        System.out.println("事务:" + event.getTransactionKey());
    }
});
```

### 推送到消息队列

```java
ctx.addEventPusher((jdbcQuery, events) -> {
    for (DBEvent event : events) {
        Map<String, Object> message = new HashMap<>();
        message.put("table", event.getTableName());
        message.put("type", event.getType().name());
        message.put("data", event.getData());
        message.put("primaryKeys", event.getPrimaryKeys());
        message.put("transactionKey", event.getTransactionKey());
        message.put("timestamp", event.getTimestamp());
        // 发送到 Kafka/RabbitMQ 等
        mqProducer.send(JSON.toJSONString(message));
    }
});
```

### 在 push 中查询补充数据

```java
ctx.addEventPusher((jdbcQuery, events) -> {
    for (DBEvent event : events) {
        if (event.isUpdate() && "t_order".equals(event.getTableName())) {
            // 利用 JdbcQuery 查询关联数据
            List<Map<String, Object>> extra = jdbcQuery.query(
                "SELECT * FROM t_order_item WHERE order_id = ?",
                Collections.singletonList(event.getData().get("id"))
            );
            // 合并发送
        }
    }
});
```

### 多推送者注册

```java
// 可注册多个推送者，全部都会被调用
ctx.addEventPusher(new ElasticsearchSyncPusher());
ctx.addEventPusher(new AuditLogPusher());
ctx.addEventPusher(new CacheInvalidationPusher());

// 清除所有推送者
ctx.cleanEventPushers();
```

## 注意事项

- **事务边界**: auto-commit 模式下每条 SQL 执行后立即触发 `push()`；手动事务模式下事件在 `commit()` 时批量推送，`rollback()` 时丢弃。推送者实现不需要关心事务边界
- **事务标识**: 每个 `DBEvent` 的 `transactionKey` 标识其所属事务，同一事务内的多个事件共享相同的 transactionKey，可用于保证消费端的顺序性和原子性
- **多推送者**: 支持注册多个 `DBEventPusher`，所有注册者都会收到全部事件。使用 `CopyOnWriteArrayList` 存储，支持运行时注册
- **默认行为**: 未注册任何推送者时，`DefaultDBEventPusher` 将事件打印到 stdout。注册至少一个推送者后，默认行为不再触发
- **线程安全**: `TransactionEventPools` 使用 ThreadLocal 隔离事务状态，推送者实现无需考虑多线程竞争。但 `push()` 回调内应避免耗时操作，以免阻塞数据库连接
- **JdbcQuery 使用**: `push()` 回调中的 `JdbcQuery` 参数使用真实（非代理）Connection 执行查询，不会触发事件递归
- **push 异常**: `push()` 中抛出异常会影响后续推送者的调用。建议在推送者内部做异常捕获和容错处理，避免影响其他推送者

## 变更说明

- **2026-04-28**: 同步最新代码，新增 `DBEvent.set()`、`DBEvent.addPrimaryKey()` 方法文档；新增 `TransactionEventPools.clear()`、`TransactionEventPools.reset()` 方法文档；Maven 坐标版本号更新为 1.0.18
