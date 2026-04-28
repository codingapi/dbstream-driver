---
module: dbstream-driver
name: DBEventSupporter
description: 表级事件过滤接口，用于控制哪些数据库表启用数据变更事件捕获，默认全部支持。
---

# DBEventSupporter

- **来源**: 自有
- **所属 module**: dbstream-driver
- **Maven 坐标**: com.codingapi.dbstream:dbstream-driver:1.0.17

## 何时使用

需要精确控制哪些数据库表参与 CDC 事件捕获时使用。默认情况下（`DefaultDBEventSupporter`）所有具备主键的表都会被捕获。当只关心部分核心业务表的变更（如订单、支付、库存），或需要排除日志表、临时表等高频低价值表时，实现此接口进行过滤。

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
| `DBEventSupporter` | `com.codingapi.dbstream.supporter` | 表级事件过滤接口 |
| `DefaultDBEventSupporter` | `com.codingapi.dbstream.supporter` | 默认实现，始终返回 true |
| `DBStreamContext` | `com.codingapi.dbstream` | 全局入口，通过 `setDbEventSupporter()` 注入自定义实现 |
| `DbTable` | `com.codingapi.dbstream.scanner` | 表元数据（表名、列、主键），过滤判断的输入参数 |
| `DbColumn` | `com.codingapi.dbstream.scanner` | 列元数据（列名、类型、是否主键） |

### 关键方法

#### DBEventSupporter 接口

| 方法签名 | 参数说明 | 返回值 | 说明 |
|----------|----------|--------|------|
| `support(Properties, DbTable)` | info=数据库连接属性；dbTable=表元数据 | boolean | 返回 true 表示对该表启用事件捕获，false 表示跳过 |

#### DbTable 判断方法（用于过滤逻辑）

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `getName()` | String | 表名称 |
| `getComment()` | String | 表注释 |
| `getColumns()` | List\<DbColumn\> | 全部列元数据 |
| `getPrimaryKeys()` | List\<String\> | 主键列名列表 |
| `hasColumns()` | boolean | 是否有列定义 |
| `hasPrimaryKeys()` | boolean | 是否有主键 |

#### 注册方式

通过 `DBStreamContext` 设置：

| 方法签名 | 说明 |
|----------|------|
| `DBStreamContext.getInstance().setDbEventSupporter(DBEventSupporter)` | 设置自定义过滤器 |
| `DBStreamContext.getInstance().support(Properties, DbTable)` | 内部调用，先检查表是否有列和主键，再委托给 supporter |

### 过滤执行时机

`DBEventSupporter.support()` 在每次 SQL 执行前的 `DBEventListener.before()` 中被调用，流程为：

1. SQL 解析器提取表名
2. 从 `DBMetaData` 查找 `DbTable`
3. 检查表是否有列和主键（前置条件）
4. 调用 `DBEventSupporter.support(properties, dbTable)` 判断
5. 返回 false → 跳过该表的事件解析，无性能开销

### 配置项

本组件无独立配置项。通过 `DBStreamContext.setDbEventSupporter()` 代码注入。

## 使用示例

### 基础用法：按表名前缀过滤

```java
DBStreamContext.getInstance().setDbEventSupporter((info, dbTable) -> {
    // 仅捕获 t_ 前缀的业务表
    return dbTable.getName().startsWith("t_");
});
```

### 按表名白名单过滤

```java
Set<String> targetTables = new HashSet<>(Arrays.asList(
    "t_order", "t_order_item", "t_payment", "t_inventory"
));

DBStreamContext.getInstance().setDbEventSupporter((info, dbTable) -> {
    return targetTables.contains(dbTable.getName().toLowerCase());
});
```

### 按数据源过滤

```java
DBStreamContext.getInstance().setDbEventSupporter((info, dbTable) -> {
    String jdbcUrl = info.getProperty("jdbc.url");
    // 只对订单库启用事件捕获
    if (jdbcUrl != null && jdbcUrl.contains("order-db")) {
        return dbTable.getName().startsWith("t_");
    }
    return false;
});
```

### 结合表结构过滤

```java
DBStreamContext.getInstance().setDbEventSupporter((info, dbTable) -> {
    // 排除没有注释的表（通常是临时表或内部表）
    if (dbTable.getComment() == null || dbTable.getComment().isEmpty()) {
        return false;
    }
    // 排除日志类表
    if (dbTable.getName().toUpperCase().contains("LOG")) {
        return false;
    }
    return true;
});
```

## 注意事项

- **前置条件**: `DBStreamContext.support()` 在调用 `DBEventSupporter` 之前会先检查表是否有列和主键。无主键的表（如日志表、关联表）不会被捕获，即使 supporter 返回 true
- **设置时机**: 建议在应用启动阶段一次性设置，`setDbEventSupporter()` 不是线程安全的
- **默认行为**: 未设置自定义 supporter 时，使用 `DefaultDBEventSupporter`（始终返回 true）。设置后会覆盖默认实现
- **性能影响**: `support()` 在每次 INSERT/UPDATE/DELETE 执行前被调用，实现应保持轻量，避免 IO 操作或复杂计算
- **null 安全**: `support()` 的两个参数不会为 null（在 `DBStreamContext.support()` 中已做前置校验）
- **单实例**: 全局只能设置一个 `DBEventSupporter`，后设置的会覆盖先前的。如需组合多个过滤条件，在实现内部组合
