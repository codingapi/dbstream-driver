package com.codingapi.dbstream;

import com.codingapi.dbstream.listener.SQLRunningContext;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import com.codingapi.dbstream.supporter.DBEventSupporter;
import com.codingapi.dbstream.supporter.DefaultDBEventSupporter;
import com.codingapi.dbstream.scanner.DBMetaContext;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusherContext;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.event.DBEventSkipContext;
import com.codingapi.dbstream.event.EventType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * DBStream 对外提供的能力服务
 */
public class DBStreamContext {

    @Getter
    private final static DBStreamContext instance = new DBStreamContext();

    @Setter
    private DBEventSupporter dbEventSupporter;


    private DBStreamContext() {

    }

    /**
     * 添加SQL执行订阅
     *
     * @param sqlExecuteListener 订阅
     */
    public void addListener(SQLExecuteListener sqlExecuteListener) {
        SQLRunningContext.getInstance().addListener(sqlExecuteListener);
    }


    /**
     * 清空所有自定义的SQL执行订阅
     */
    public void cleanCustomListeners(){
        SQLRunningContext.getInstance().cleanCustomListeners();
    }


    /**
     * 是否支持SQL 拦截代理分析
     *
     * @param info    数据库连接信息
     * @param dbTable 数据库表信息
     * @return 是否支持
     */
    public boolean support(Properties info, DbTable dbTable) {
        if (dbEventSupporter == null) {
            this.dbEventSupporter = new DefaultDBEventSupporter();
        }
        if (dbTable.hasColumns() && dbTable.hasPrimaryKeys()) {
            return dbEventSupporter.support(info, dbTable);
        } else {
            return false;
        }
    }


    /**
     * 添加DB事件推送
     *
     * @param dbEventPusher DB事件推送者
     */
    public void addEventPusher(DBEventPusher dbEventPusher) {
        DBEventPusherContext.getInstance().addPusher(dbEventPusher);
    }


    /**
     * 清空DB事件推送
     */
    public void cleanEventPushers() {
        DBEventPusherContext.getInstance().clean();
    }


    /**
     * 跳过下一条匹配语句的全部事件（不限事件类型）。
     * <p>
     * 一次性消费语义：标记对当前线程下一条匹配该表的 DML 语句生效后自动失效；
     * 语句级跳过在 SQL 执行前置阶段直接短路，不做前镜像查询，不产生任何事件（数据照常入库）。
     * <p>
     * 注意：调用本方法的线程必须与执行 SQL 的线程一致。
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     */
    public void skipNextEvent(String tableName) {
        DBEventSkipContext.getInstance().skipNextEvent(tableName);
    }

    /**
     * 跳过下一条匹配语句的全部事件（限定事件类型）。
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     * @param type      事件类型，null 表示所有类型
     */
    public void skipNextEvent(String tableName, EventType type) {
        DBEventSkipContext.getInstance().skipNextEvent(tableName, type);
    }

    /**
     * 按数据列值匹配跳过事件（行级）。
     * <p>
     * 一次性消费语义：事件生成后逐条匹配，实际抑制了至少一个事件才消费该标记；
     * 同一语句（含批量、多行VALUES）内所有命中的事件都会被跳过。
     * 一直未命中的标记会保留到 {@link #clearSkipEvents()} 被调用或数据库连接关闭时。
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     * @param type      事件类型，null 表示所有类型
     * @param dataMatch 数据列值匹配条件（列名忽略大小写，值兼容数值类型差异），null 或空表示无条件
     */
    public void skipNextEvent(String tableName, EventType type, Map<String, Object> dataMatch) {
        DBEventSkipContext.getInstance().skipNextEvent(tableName, type, dataMatch);
    }

    /**
     * 按自定义断言跳过事件（行级）。
     * <p>
     * 一次性消费语义同 {@link #skipNextEvent(String, EventType, Map)}。
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     * @param type      事件类型，null 表示所有类型
     * @param predicate 事件断言，返回 true 表示跳过该事件
     */
    public void skipNextEvent(String tableName, EventType type, Predicate<DBEvent> predicate) {
        DBEventSkipContext.getInstance().skipNextEvent(tableName, type, predicate);
    }

    /**
     * 清空当前线程的所有事件跳过标记
     */
    public void clearSkipEvents() {
        DBEventSkipContext.getInstance().clear();
    }

    /**
     * 获取元数据库的信息
     *
     * @param jdbcKey 元数据信息key
     * @return 元数据信息
     */
    public DBMetaData getMetaData(String jdbcKey) {
        return DBMetaContext.getInstance().getMetaData(jdbcKey);
    }

    /**
     * 获取数据库连接信息
     *
     * @return 数据连接信息
     */
    public List<String> loadDbKeys() {
        return DBMetaContext.getInstance().loadDbKeys();
    }


    /**
     * 返回元数据信息列表
     *
     * @return 元数据信息列表
     */
    public List<DBMetaData> metaDataList() {
        return DBMetaContext.getInstance().metaDataList();
    }


    /**
     * 清空数据库元数据，数据清空以后下次执行数据库访问时会自己重新加载元数据。
     */
    public void clearAll() {
        DBMetaContext.getInstance().clearAll();
    }


    /**
     * 通过jdbcUrl {@link DBMetaData#KEY_JDBC_KEY} 清空指定数据库的元数据信息。
     * 可通过 {@link DBMetaContext#loadDbKeys()} 查看数据库的jdbcKey信息。
     * 数据清空以后下次执行数据库访问时会自己重新加载元数据。
     *
     * @param jdbcKey 数据库key
     */
    public void clear(String jdbcKey) {
        DBMetaContext.getInstance().clear(jdbcKey);
    }

    /**
     * 刷新指定数据源中指定表的元数据。
     * 适用于运行时动态创建或修改表结构后，手动触发元数据更新。
     *
     * @param connection 数据库连接
     * @param jdbcKey    数据源唯一标识，可通过 {@link #loadDbKeys()} 获取
     * @param tableName  需要刷新的表名称
     * @throws SQLException 刷新失败时抛出
     */
    public void refreshTable(Connection connection, String jdbcKey, String tableName) throws SQLException {
        DBMetaContext.getInstance().refreshTable(jdbcKey, connection, tableName);
    }

    /**
     * 全量刷新指定数据源的元数据。
     * 重新扫描所有表结构并更新缓存。
     *
     * @param connection 数据库连接
     * @param jdbcKey    数据源唯一标识，可通过 {@link #loadDbKeys()} 获取
     * @throws SQLException 刷新失败时抛出
     */
    public void refreshAll(Connection connection, String jdbcKey) throws SQLException {
        DBMetaContext.getInstance().refreshAll(jdbcKey, connection);
    }


}
