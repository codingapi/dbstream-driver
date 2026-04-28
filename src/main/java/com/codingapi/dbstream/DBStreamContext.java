package com.codingapi.dbstream;

import com.codingapi.dbstream.listener.SQLRunningContext;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import com.codingapi.dbstream.supporter.DBEventSupporter;
import com.codingapi.dbstream.supporter.DefaultDBEventSupporter;
import com.codingapi.dbstream.scanner.DBMetaContext;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.event.DBEventContext;
import com.codingapi.dbstream.event.DBEventPusher;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

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
        DBEventContext.getInstance().addPusher(dbEventPusher);
    }


    /**
     * 清空DB事件推送
     */
    public void cleanEventPushers() {
        DBEventContext.getInstance().clean();
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
