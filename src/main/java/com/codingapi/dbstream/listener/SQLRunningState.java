package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.proxy.ConnectionProxy;
import com.codingapi.dbstream.query.JdbcQuery;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DBScanner;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * SQL执行状态
 */
public class SQLRunningState {

    /**
     * 执行SQL参数队列
     */
    private final List<SQLRunningParam> sqlRunningParams;

    /**
     * 当前执行SQL的参数对象
     */
    private SQLRunningParam currentParam;

    /**
     * 批量模式标识
     */
    @Getter
    private boolean batchMode = false;

    /**
     * 当前绑定sql
     */
    @Getter
    private String sql;

    /**
     * 数据库的元数据信息
     */
    @Getter
    private final DBMetaData metaData;

    /**
     * 数据库的当前statement对象
     */
    @Getter
    private final Statement statement;


    /**
     * 数据库的当前链接对象
     */
    @Getter
    private final ConnectionProxy connectionProxy;

    /**
     * sql执行结果
     */
    @Setter
    @Getter
    private Object result;

    /**
     * 开始执行SQL时间戳
     */
    @Getter
    private long beginTimestamp;

    /**
     * 结束执行SQL时间戳
     */
    @Getter
    private long afterTimestamp;

    /**
     * JDBC数据查询对象
     */
    @Getter
    private final JdbcQuery jdbcQuery;


    public SQLRunningState(String sql, ConnectionProxy connectionProxy, Statement statement, DBMetaData metaData) {
        this.sql = sql;
        this.connectionProxy = connectionProxy;
        this.statement = statement;
        this.jdbcQuery = new JdbcQuery(connectionProxy);
        this.metaData = metaData;
        this.sqlRunningParams = new ArrayList<>();

        this.currentParam = new SQLRunningParam();
        this.currentParam.setSql(sql);
        this.sqlRunningParams.add(currentParam);
    }

    public void setSql(String sql) {
        this.sql = sql;
        if (this.currentParam != null) {
            this.currentParam.setSql(sql);
        }
    }

    /**
     * 添加任务队列
     *
     * @param sql 执行sql
     */
    public void addBatch(String sql) {
        batchMode = true;
        SQLRunningParam executeParam = new SQLRunningParam();
        executeParam.setSql(sql);
        this.sqlRunningParams.add(executeParam);
        this.currentParam = executeParam;
    }

    /**
     * 添加任务队列
     *
     */
    public void addBatch() {
        this.addBatch(this.sql);
    }

    /**
     * 清空队列
     */
    public void clearBatch() {
        this.sqlRunningParams.clear();
        this.currentParam = null;
    }

    /**
     * 清理参数设置
     */
    public void cleanParams() {
        if (this.currentParam != null) {
            this.currentParam.cleanParams();
        }
    }

    /**
     * 记录执行前时间
     */
    void begin() {
        this.beginTimestamp = System.currentTimeMillis();
    }

    /**
     * 记录执行后时间
     */
    void after() {
        this.afterTimestamp = System.currentTimeMillis();
    }


    /**
     * SQL执行总耗时
     *
     * @return 执行耗时
     */
    public long getExecuteTimestamp() {
        return this.afterTimestamp - this.beginTimestamp;
    }

    /**
     * 更新sql参数
     *
     * @param key   参数key
     * @param value 参数值
     */
    public void setParam(String key, Object value) {
        if (this.currentParam != null) {
            currentParam.setParam(key, value);
        }
    }

    /**
     * 更新sql参数
     *
     * @param index 参数索引
     * @param value 参数值
     */
    public void setParam(int index, Object value) {
        if (this.currentParam != null) {
            currentParam.setParam(index, value);
        }
    }

    /**
     * 获取参数列表
     *
     * @return List
     */
    public List<Object> getListParams() {
        if (batchMode) {
            if (this.sqlRunningParams.isEmpty()) {
                return new ArrayList<>();
            }
            int size = this.sqlRunningParams.size();
            return this.sqlRunningParams.get(size - 2).getListParams();
        }
        if (this.currentParam != null) {
            return currentParam.getListParams();
        }
        return new ArrayList<>();
    }


    /**
     * 获取Batch的SQLRunningState
     *
     * @return List
     */
    public List<SQLRunningState> getBatchSQLRunningStateList() {
        if (this.batchMode) {
            if (this.sqlRunningParams.isEmpty()) {
                return new ArrayList<>();
            }
            int size = this.sqlRunningParams.size();
            List<SQLRunningState> list = new ArrayList<>();
            List<SQLRunningParam> paramList = this.sqlRunningParams.subList(0, size - 1);
            for (SQLRunningParam executeParam : paramList) {
                SQLRunningState executeState = new SQLRunningState(executeParam.getSql(), connectionProxy, statement, metaData);
                executeState.currentParam = executeParam;
                list.add(executeState);
            }
            return list;

        }
        return new ArrayList<>();
    }


    /**
     * 元数据中是否存在表
     *
     * @param tableName 表名称
     * @return true 存在
     */
    public DbTable getDbTable(String tableName) {
        return this.metaData != null ? this.metaData.getTable(tableName) : null;
    }

    /**
     * 获取事务标识信息
     */
    public String getTransactionKey() {
        if (this.connectionProxy != null) {
            return this.connectionProxy.getTransactionKey();
        }
        return null;
    }

    /**
     * 查询
     *
     * @param sql sql
     * @return 查询结果
     * @throws SQLException 查询异常
     */
    public List<Map<String, Object>> query(String sql) throws SQLException {
        return this.jdbcQuery.query(sql);
    }

    /**
     * 查询
     *
     * @param sql    sql
     * @param params 参数
     * @return 查询结果
     * @throws SQLException 查询异常
     */
    public List<Map<String, Object>> query(String sql, List<Object> params) throws SQLException {
        return jdbcQuery.query(sql, params);
    }

    /**
     * 获取 statement 返回的keys
     *
     * @return list
     */
    public List<Map<String, Object>> getStatementGenerateKeys(DbTable dbTable) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSet rs = this.statement.getGeneratedKeys();
            if (rs != null) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    ResultSetMetaData resultSetMetaData = rs.getMetaData();
                    int columnCount = resultSetMetaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columName = resultSetMetaData.getColumnName(i);
                        DbColumn dbColumn = dbTable.getColumnByName(columName);
                        if (dbColumn != null) {
                            map.put(dbColumn.getName(), rs.getObject(i));
                        }
                    }
                    list.add(map);
                }
            }
        } catch (SQLException ignored) {
        }
        return list;
    }


    /**
     * 获取驱动配置信息
     *
     * @return Properties
     */
    public Properties getDriverProperties() {
        if (metaData == null) {
            return null;
        }
        return this.metaData.getProperties();
    }


    /**
     * 获取数据库的jdbcUrl
     *
     * @return jdbcUrl
     */
    public String getJdbcUrl() {
        if (metaData == null) {
            return null;
        }
        return metaData.getJdbcUrl();
    }


    /**
     * 获取数据库的jdbcKey
     *
     * @return jdbcKey
     */
    public String getJdbcKey() {
        if (metaData == null) {
            return null;
        }
        return metaData.getKeyJdbcKey();
    }


    /**
     * 更新数据库的元数据信息
     *
     * @param tableName 表名
     */
    public void triggerDBMetaData(String tableName) throws SQLException {
        // 当前表需要更新时，将会连同所有带更新的表一次性全部更新
        if (this.metaData.isSubjectUpdate(tableName)) {
            DBScanner dbScanner = new DBScanner(connectionProxy.getConnection(), getDriverProperties());
            dbScanner.updateMetadata(this.metaData);
        }
    }
}
