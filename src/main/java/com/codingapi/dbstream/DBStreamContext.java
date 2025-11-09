package com.codingapi.dbstream;

import com.codingapi.dbstream.interceptor.SQLRunningContext;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import com.codingapi.dbstream.provider.DBTableSupportProvider;
import com.codingapi.dbstream.provider.DefaultDBTableSupportProvider;
import com.codingapi.dbstream.scanner.DBMetaContext;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.stream.DBEventContext;
import com.codingapi.dbstream.stream.DBEventPusher;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Properties;

/**
 * DBStream 对外提供的能力服务
 */
public class DBStreamContext {

    @Getter
    private final static DBStreamContext instance = new DBStreamContext();

    @Setter
    private DBTableSupportProvider dbTableSupportProvider;


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
     * 是否支持SQL 拦截代理分析
     * @param info 数据库连接信息
     * @param tableName 数据库名称
     * @return 是否支持
     */
    public boolean support(Properties info, String tableName) {
        if (dbTableSupportProvider == null) {
            this.dbTableSupportProvider = new DefaultDBTableSupportProvider();
        }
        return dbTableSupportProvider.support(info, tableName);
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

}
