# 调整计划: 动态表元数据刷新机制

> 编码: 819340 | 日期: 2026-04-28 | 类型: 调整任务 | 来源: change | 基于: docs/changes/819340-dynamic-table-metadata-refresh.md

## 调整目标

为 DBStream 提供主动刷新 DBMetaData 缓存的公共 API，支持按表名或全量刷新元数据，使运行时动态创建或修改的表能被业务方手动触发更新后纳入 CDC 事件捕获。

## 影响面（基于指令结果）

- 涉及 Maven 模块: dbstream-driver（单模块工程，`mvn help:evaluate` 确认 artifactId=dbstream-driver）
- 直接依赖者: 无（单模块工程，无 reactor 内部依赖）
- 外部 API / 公共组件影响: 是 — 新增公共 API 方法到 `DBStreamContext` 和 `DBMetaContext`，现有 API 行为不变

## 分步策略

### Step 1: 在 DBScanner 中新增单表扫描方法

新增 `scanTable(String tableName)` 方法，扫描指定单张表的元数据（列、主键），返回 `DbTable`。复用现有的 `loadDbTableInfo` 逻辑。

### Step 2: 在 DBMetaData 中新增主动刷新方法

新增 `refreshTable(ConnectionProxy, String tableName)` 方法：扫描指定表元数据并更新到 tables 列表。若表已存在则替换，不存在则追加。同时更新该表的本地序列化缓存。

新增 `refreshAll(ConnectionProxy)` 方法：重新扫描全部表，替换整个 tables 列表。

### Step 3: 在 DBMetaContext 中暴露刷新 API

新增 `refreshTable(String jdbcKey, ConnectionProxy, String tableName)` 和 `refreshAll(String jdbcKey, ConnectionProxy)` 方法，委托给 `DBMetaData` 执行。

### Step 4: 在 DBStreamContext 中暴露公共 API

新增对外方法：
- `refreshTable(String jdbcKey, String tableName)` — 刷新指定表的元数据
- `refreshAll(String jdbcKey)` — 全量刷新指定数据源的元数据

### Step 5: 编写测试用例

验证手动调用 `refreshTable` / `refreshAll` 后，新表能被 CDC 捕获，修改后的列结构被正确使用。

## 新增文件

| 文件路径 | 用途说明 |
|----------|----------|
| `src/test/java/com/example/dbstream/tests/MetadataRefreshTest.java` | 手动刷新元数据的集成测试 |

## 修改文件

| 文件路径 | 修改内容 |
|----------|----------|
| `src/main/java/com/codingapi/dbstream/scanner/DBScanner.java` | 新增 `scanTable(String tableName)` 方法，扫描单表元数据 |
| `src/main/java/com/codingapi/dbstream/scanner/DBMetaData.java` | 新增 `refreshTable(ConnectionProxy, String)` 和 `refreshAll(ConnectionProxy)` 方法 |
| `src/main/java/com/codingapi/dbstream/scanner/DBMetaContext.java` | 新增 `refreshTable` / `refreshAll` 方法 |
| `src/main/java/com/codingapi/dbstream/DBStreamContext.java` | 新增 `refreshTable(String jdbcKey, String tableName)` 和 `refreshAll(String jdbcKey)` 公共 API |

## 移除文件（若有）

无。

## 兼容性与迁移

- 保留的 API / 行为: 所有现有公共 API（`clear`/`clearAll`/`addUpdateSubscribe`/`getTable` 等）行为不变
- 破坏性变更: 无
- 数据/配置迁移: 无。现有 `.dbstream/` 本地缓存格式不变，`refreshTable` 会自动更新缓存文件

## 核验机制

| 验证项 | 说明 |
|--------|------|
| 编译 | `./mvnw compile` |
| 单测 | `./mvnw test` — 包含新增的元数据刷新测试和全部既有测试 |
| 打包 | `./mvnw package` |
| 行为回归 | 现有 User1/User2/User3 RepositoryTest 全部通过；新增 MetadataRefreshTest 验证手动刷新场景 |

具体验证命令：

```bash
./mvnw compile
./mvnw test
./mvnw package
```

## 执行顺序

1. 修改 `src/main/java/com/codingapi/dbstream/scanner/DBScanner.java` — 新增 `scanTable(String)` 方法
2. 修改 `src/main/java/com/codingapi/dbstream/scanner/DBMetaData.java` — 新增 `refreshTable()` 和 `refreshAll()` 方法
3. 修改 `src/main/java/com/codingapi/dbstream/scanner/DBMetaContext.java` — 新增 `refreshTable()` 和 `refreshAll()` 方法
4. 修改 `src/main/java/com/codingapi/dbstream/DBStreamContext.java` — 新增公共 API
5. 创建 `src/test/java/com/example/dbstream/tests/MetadataRefreshTest.java` — 集成测试
6. 执行 `./mvnw test` 验证

## 变更说明

**调整意见原文**: "仅提供提供主动刷新 API（按表名或全量刷新）的能力即可"

**调整原因**: 缩小实现范围，仅提供手动刷新 API。移除了原计划中的 DDL 自动检测相关步骤（原 Step 1 SQLUtils DDL 检测、原 Step 6 DDLMetadataRefreshListener、原 Step 7 注册 DDL 监听器），以及对应的测试文件 DDLMetadataRefreshTest 和对 SQLUtils / SQLRunningContext 的修改。DDL 自动检测可作为后续独立任务实现。
