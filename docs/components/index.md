# 组件索引

> 本文档由 `/rebuild-components-index` 自动生成。每次 `/write-components` 完成后应自动重建。
> 手动维护本文件会被下一次重建覆盖。

| 组件模块 | 组件名称 | 组件描述（应用场景） | 文档地址 |
|----------|----------|----------------------|----------|
| dbstream-driver | DBEventPusher | 数据变更事件推送接口，在事务提交时接收 DBEvent 列表并推送到外部系统（消息队列、日志、缓存同步等）。 | [dbstream-driver_DBEventPusher](./dbstream-driver_DBEventPusher.md) |
| dbstream-driver | DBEventSupporter | 表级事件过滤接口，用于控制哪些数据库表启用数据变更事件捕获，默认全部支持。 | [dbstream-driver_DBEventSupporter](./dbstream-driver_DBEventSupporter.md) |
| dbstream-driver | DBStreamContext | DBStream 的全局统一入口，用于注册 SQL 执行监听器、事件推送者、表级过滤器，以及管理数据库元数据缓存。 | [dbstream-driver_DBStreamContext](./dbstream-driver_DBStreamContext.md) |
| dbstream-driver | SQLExecuteListener | SQL 执行拦截监听接口，允许在 SQL 执行前后插入自定义逻辑，如日志记录、性能监控、审计追踪等。 | [dbstream-driver_SQLExecuteListener](./dbstream-driver_SQLExecuteListener.md) |
