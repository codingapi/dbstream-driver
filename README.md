[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/codingapi/dbstream-driver/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.codingapi.dbstream/dbstream-driver.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.codingapi.dbstream%22%20AND%20a:%22dbstream-driver%22)

# dbstream-driver

一个基于 JDBC 代理驱动的数据库变更事件推送框架，通过代理 JDBC 驱动实现监控数据库的持久化操作，自动捕获 INSERT/UPDATE/DELETE 操作并推送结构化数据变更事件。本框架支持JDK1.8及以上的java环境。

## ✨ 核心特性

### 🚀 无侵入性（Non-invasive）
- **零代码修改**：无需修改任何业务代码，只需替换 JDBC 驱动类名
- **透明代理**：自动委派给真实 JDBC 驱动，保持原有功能完全不变
- **配置即用**：修改数据源配置即可启用，无需额外代码

### 💡 方便性（Convenience）
- **自动元数据扫描**：首次连接自动扫描并缓存数据库表结构、字段、主键等元数据信息
- **内置解析器**：内置 INSERT/UPDATE/DELETE SQL 解析器，自动提取变更数据
- **事务感知**：自动识别事务边界，支持自动提交和手动事务模式
- **多数据源支持**：通过 jdbcKey 区分不同数据源，支持多数据源场景

### 🔧 扩展性（Extensibility）
- **监听器机制**：支持自定义 `SQLExecuteListener` 监听 SQL 执行前后事件
- **事件推送器**：支持自定义 `DBEventPusher` 实现事件推送逻辑（如对接消息队列）
- **插件化架构**：基于接口设计，易于扩展和定制


## 📋 应用场景

- **数据宽表查询优化**：实时同步数据到宽表，提升查询性能
- **统一数据统计口径**：实时捕获数据变更，统一数据统计逻辑
- **数据实时备份**：监听数据变更，实现实时数据备份
- **数据缓存同步**：数据库变更时自动更新缓存
- **数据变更审计**：记录所有数据变更操作，用于审计和追溯
- **数据同步**：实现数据库之间的实时数据同步

## 🔧 依赖环境

- **JDK**: 8 +
- **Maven**: 3.8 +

## 🚀 快速开始

### 1. 引入依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.codingapi.dbstream</groupId>
    <artifactId>dbstream-driver</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 配置数据源

将数据源驱动类配置为 `com.codingapi.dbstream.driver.DBStreamProxyDriver`，URL 保持原有 JDBC URL 不变：

**Spring Boot 配置示例（application.properties）：**

```properties
spring.datasource.driver-class-name=com.codingapi.dbstream.driver.DBStreamProxyDriver
spring.datasource.url=jdbc:mysql://localhost:3306/example?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=12345678
```

**说明**：代理驱动会在运行时自动识别 JDBC URL 并委派给真实 JDBC 驱动（如 MySQL、PostgreSQL、H2 等），无需额外配置。

### 3. 订阅数据库变更事件（可选）

实现并注册 `DBEventPusher` 接口，接收结构化的数据库变更事件：

```java
import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.DBEventPusher;

// 在应用启动时注册事件推送器（如 @PostConstruct、@Configuration 等）
DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
    @Override
    public void push(List<DBEvent> events) {
        // 处理数据库变更事件
        for (DBEvent event : events) {
            System.out.println("表名: " + event.getTableName());
            System.out.println("操作类型: " + event.getType()); // INSERT/UPDATE/DELETE
            System.out.println("变更数据: " + event.getData());
            System.out.println("主键: " + event.getPrimaryKeys());
            System.out.println("事务标识: " + event.getTransactionKey());
            System.out.println("时间戳: " + event.getTimestamp());
            
            // 可以对接消息队列（如 Kafka、RocketMQ 等）
            // kafkaProducer.send(event);
        }
    }
});
```

### 4. 订阅 SQL 执行回调（可选）

实现并注册 `SQLExecuteListener`，可以获取原始 SQL 和参数信息：

```java
import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.listener.SQLExecuteListener;

public class MySQLListener implements SQLExecuteListener {
    @Override
    public void before(SQLExecuteState executeState) {
        System.out.println("执行前 - SQL: " + executeState.getSql());
        System.out.println("执行前 - 参数: " + executeState.getListParams());
    }

    @Override
    public void after(SQLExecuteState executeState, Object result) {
        System.out.println("执行后 - SQL: " + executeState.getSql());
        System.out.println("执行后 - 参数: " + executeState.getListParams());
        System.out.println("执行后 - 耗时: " + executeState.getExecuteTimestamp());
        System.out.println("执行后 - 结果: " + result);
    }
}

// 注册监听器
DBStreamContext.getInstance().addListener(new MySQLListener());
```

### 5. 通过设置DBTableSupportProvider订阅对那些表进行监听（可选）

```
 
import com.codingapi.dbstream.scanner.DbTable;

import java.util.Properties;

public class DefaultDBTableSupportProvider implements DBTableSupportProvider {

    @Override
    public boolean support(Properties info, DbTable dbTable) {
       // 所有表都会监听
       return true;
    }
}

// 添加 SQL 表执行判断
DBStreamContext.getInstance().setDbTableSupportProvider(new DefaultDBTableSupportProvider());
```

根据表名等信息来决定是否进行数据事件解析。仅当返回true的才会进行事件推送。DefaultDBTableSupportProvider为默认的实现机制。

### 6. 查看表或情况表缓存数据（可选）

在项目启动以后，会在项目的根路径下创建.dbstream文件夹，文件夹中存储的内容为数据库的表扫描缓存数据。  
文件夹的名称为jdbcKey的字段，文件夹下的内容为缓存的表结构信息，当表结构发生变化以后可以删除对应的文件进行更新。  
也可以在系统中通过执行 `DBStreamContext.getInstance().clear(String jdbcKey);`进行情况数据。   
jdbcKey是通过sha256(jdbcUrl+schema)计算得来。
```
.
└── beefae7e00deb825a3a591ab7a22791a4df799afba9fed71f8b549665508c7ee
    └── M_USER

```

### 7. 主键关系手动维护（可选）

在数据库中存在不存在物理的主键字段，但是存在业务主键字段，可通过手动配置的方式，手动标记字段为主键字段。在扫描后的配置文件下增加对应表名的.key文件，例如：M_USER.key
```
.
└── beefae7e00deb825a3a591ab7a22791a4df799afba9fed71f8b549665508c7ee
    ├── M_USER
    ├── M_USER.key
    └── M_USER_2
```

写法如下，填写字段的名称，多个用英文,分割。
```
USERNAME,ID
```

## 📖 API 文档

### DBStreamContext

框架的核心上下文类，提供所有对外能力：

#### 事件推送相关

```java
// 添加数据库事件推送器
DBStreamContext.getInstance().addEventPusher(DBEventPusher pusher);
```

#### 监听器相关

```java
// 添加 SQL 执行监听器
DBStreamContext.getInstance().addListener(SQLExecuteListener listener);
```

#### 数据库表支持判断

```java
// 添加 SQL 表执行判断
DBStreamContext.getInstance().setDbTableSupportProvider(DBTableSupportProvider dbTableSupportProvider);
```

#### 元数据管理

```java
// 获取所有数据库的元数据信息列表
List<DBMetaData> metaDataList = DBStreamContext.getInstance().metaDataList();

// 通过 jdbcKey 获取指定数据库的元数据信息
DBMetaData metaData = DBStreamContext.getInstance().getMetaData(String jdbcKey);

// 主动更新表的元数据信息，在使用改变时将会先更新metadata然后再执行业务。（动态更新表的元数据）
metaData.addUpdateTableMateList(String tableName);

// 获取所有已缓存的数据库连接 jdbcKey 列表
List<String> dbKeys = DBStreamContext.getInstance().loadDbKeys();

// 清理指定数据库的元数据缓存（清空后下次访问会自动重新加载，动态更新表的元数据）
DBStreamContext.getInstance().clear(String jdbcKey);

// 清理所有数据库的元数据缓存（动态更新表的元数据）
DBStreamContext.getInstance().clearAll();
```

### DBEvent 事件模型

数据库变更事件包含以下信息：

- `tableName`: 表名
- `type`: 操作类型（INSERT/UPDATE/DELETE）
- `data`: 变更的数据（Map 格式，key 为字段名，value 为字段值）
- `primaryKeys`: 主键列表
- `jdbcUrl`: 数据库连接 URL
- `transactionKey`: 事务标识（同一事务内的操作共享相同标识）
- `timestamp`: 事件产生时间戳
- `pushTimestamp`: 事件推送时间戳

## 🧪 运行测试

项目已包含完整的单元测试示例，运行测试：

```bash
mvn clean test -P travis
```

## ⚠️ 注意事项

1. **事件推送时机**：
   - 仅在使用 `Statement`/`PreparedStatement` 执行 INSERT/UPDATE/DELETE 操作时才会产生事件
   - SELECT 查询操作不会推送事件
   - 事件推送是同步触发回调，请在实现中避免耗时阻塞，必要时交给异步处理

2. **事务支持**：
   - 框架自动识别事务边界，支持自动提交和手动事务模式
   - 手动事务模式下，事件会在 `commit()` 时批量推送
   - 事务回滚时，相关事件会被丢弃

3. **数据表限制**：
   - 执行数据拦截事件的分析，要求表必须存在主键的定义

4. **元数据缓存**：
   - 数据库元数据会在首次连接时自动扫描并缓存
   - 如果数据库表结构发生变化，可以调用 `clear()`或`metaData.addUpdateTableMateList(String tableName);` 方法清理缓存，下次访问时会自动重新加载

## 📄 许可证

本项目采用 [Apache License 2.0](./LICENSE) 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！


## 📞 联系方式

- 项目地址: https://github.com/codingapi/dbstream-driver
- 问题反馈: https://github.com/codingapi/dbstream-driver/issues
