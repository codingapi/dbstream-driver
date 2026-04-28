# 动态表元数据刷新机制

> 编码: 819340 | 日期: 2026-04-28 | 调整类型: 功能增强 | 状态: 草稿

## 调整背景

运行时动态创建或修改的表，无法主动的加载更新到缓存数据DBMetaData。当前 DBMetaData 在首次连接时通过 `DBScanner` 扫描全部表结构并缓存到 `DBMetaContext`（ConcurrentHashMap），后续不再主动更新。这意味着：

1. 应用运行期间通过 DDL（CREATE TABLE / ALTER TABLE）新增或修改的表结构不会反映到缓存中
2. 对新增表的 INSERT/UPDATE/DELETE 操作无法触发 CDC 事件（因为 `DbTable` 查找返回 null）
3. 修改了表的列结构（如新增列、修改主键）后，事件捕获仍使用旧的元数据，可能导致数据丢失或解析错误
4. 当前仅支持通过 `DBMetaData.addUpdateSubscribe()` 注册订阅表名，在下次 SQL 执行到该表时触发局部更新，但这属于被动机制，需要预先知道表名

## 关键要点

- **涉及模块**: dbstream-driver
  - `scanner/DBScanner`（元数据扫描入口）
  - `scanner/DBMetaContext`（元数据缓存单例）
  - `scanner/DBMetaData`（单数据源元数据，含 `addUpdateSubscribe` 订阅机制）
  - `proxy/ConnectionProxy`（连接代理，元数据加载触发点）
  - `DBStreamContext`（对外 API，`clear`/`clearAll` 可强制刷新缓存）

- **影响范围**:
  - 所有依赖 `DBMetaData.getTable()` 获取表信息的调用方
  - `DBEventListener.before()` 中查找 `DbTable` 的逻辑
  - `DBTableSerializableHelper` 的本地文件缓存一致性
  - 现有的 `addUpdateSubscribe` 被动订阅机制可能需要重新评估

- **现有缓解机制**:
  - `DBStreamContext.clear(jdbcKey)` / `clearAll()` 可手动清除缓存，下次连接时自动重新扫描
  - `DBMetaData.addUpdateSubscribe(tableName)` 支持注册待更新表名，在 SQL 执行到该表时被动触发 `DBScanner.findTableMetadata()` 局部更新
  - `SQLRunningState.triggerDBMetaData(tableName)` 在 `DBEventListener.before()` 中被调用，是被动更新的触发点

- **可能的改进方向**:
  - 提供主动刷新 API（按表名或全量刷新）
  - 支持 DDL 语句检测（拦截 CREATE/ALTER TABLE 自动触发元数据刷新）
  - 定时刷新机制（可选）
  - 事件驱动的刷新回调

## 备注

{待人工补充}
