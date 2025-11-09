[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/codingapi/dbstream-driver/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.codingapi.dbstream/dbstream-driver.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.codingapi.dbstream%22%20AND%20a:%dbstream-driver%22)

# dbstream-driver

dbstream-driver框架，是一个代理关系性数据库推送数据变更消息的代理框架。通过代理JDBC驱动实现监控数据库的持久化操作，然后将影响的数据推送出来，可用于数据宽表查询优化，统一数据统计口径，数据实时备份，数据缓存备份等场景

## 依赖环境

* JDK 8 +
* Maven 3.9 +

## 快速开始（集成 dbstream-driver）

以下以 Spring Boot 项目为例，展示最少集成步骤。

1) 引入依赖

在你的应用 `pom.xml` 中添加（确保能解析此模块依赖；若为多模块同仓库则按模块方式引用）,dbstream-driver支持jdk1.8及以上的版本：

```xml

<dependency>
    <groupId>com.codingapi.dbstream</groupId>
    <artifactId>dbstream-driver</artifactId>
    <version>${latest.version}</version>
</dependency>
```

2) 配置数据源使用代理驱动

将数据源驱动类配置为 `com.codingapi.dbstream.driver.DBStreamProxyDriver`，URL 仍使用原有 JDBC URL（示例为 MySQL）：

```properties
spring.datasource.driver-class-name=com.codingapi.dbstream.driver.DBStreamProxyDriver
spring.datasource.url=jdbc:mysql://localhost:3306/example?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=12345678
```

代理驱动会在运行期自动寻找并委派给真实 JDBC 驱动（通过 `DriverManager` 探测 URL），并在 SQL 执行前后注入拦截逻辑。

3) 订阅 SQL 执行回调（可选）

实现并注册 `SQLExecuteListener`，可以拿到原始 SQL 与参数：

```java
import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.listener.SQLExecuteListener;

public class MySQLListener implements SQLExecuteListener {
    @Override
    public void after(SQLExecuteState executeState, Object result) {
        System.out.println("after sql=" + executeState.getSql() + ", params=" + executeState.getListParams());
    }

    @Override
    public void before(SQLExecuteState executeState) {
        System.out.println("before sql=" + executeState.getSql() + ", params=" + executeState.getListParams());
    }
}

// 在应用启动后注册（例如 @PostConstruct、测试用 @BeforeEach 等）
DBStreamContext.getInstance().addListener(new MySQLListener());
```

4) 订阅数据库事件推送（INSERT/UPDATE/DELETE 的行级事件）

实现并注册 `DBEventPusher` 接口，即可收到解析后的结构化事件 `DBEvent` 列表：

```java
import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.DBEventPusher;

DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
    @Override
    public void push(List<DBEvent> events) {
        System.out.println(events);
    }
});
```

事件模型包含表名、变更类型、主键、列数据、数据产生时间戳以及 JDBC URL 等信息，便于对接消息中间件或下游处理逻辑。

## 运行示例

示例模块已集成上述配置，直接运行：

```bash
mvn clean test -P travis
```

## API 速览（与集成相关）

- `DBStreamContext.getInstance().addListener(SQLExecuteListener listener)`: 订阅 SQL 执行回调。
- `DBStreamContext.getInstance().addEventPusher(DBEventPusher pusher)`: 订阅结构化数据库事件。
- `DBStreamContext.getInstance().metaDataList()`: 获取已缓存的数据库元数据信息列表。
- `DBStreamContext.getInstance().getMetaData(String jdbcKey)`: 通过jdbcKey获取已缓存的数据库元数据信息列表。
- `DBStreamContext.getInstance().loadDbKeys()`: 获取已缓存的数据库连接jdbcKey信息。
- `DBStreamContext.getInstance().clear(String jdbcKey)`: 清理指定/全部数据库的元数据缓存，数据清空以后下次执行数据库访问时会自己重新加载元数据。

## 说明

- 代理驱动默认内置针对 INSERT/UPDATE/DELETE 的解析与事件推送（见 `SQLInsertExecuteListener`、`SQLUpdateExecuteListener`、
  `SQLDeleteExecuteListener`）。
- 仅在使用 `Statement`/`PreparedStatement` 执行写操作时才会产出事件；查询不会推送事件。
- 事件推送是同步触发回调，请在实现中避免耗时阻塞，必要时交给异步处理。
- 由于JDBC在执行insert into select语句时无法获取到自增的id，因此框架为对次插入方式进行支持，请在实现中避免这样的写法。


