package com.codingapi.dbstream.interceptor;

import com.codingapi.dbstream.proxy.ConnectionProxy;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DBScanner;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.*;

/**
 * 数据库的执行状态
 */
public class SQLExecuteState {

    /**
     * SQL参数,integer index模式
     */
    @Getter
    private final Map<Integer,Object> indexParams;
    /**
     * SQL参数,string key 模型
     */
    @Getter
    private final Map<String, Object> mapParams;

    /**
     * 执行的sql
     */
    @Getter
    @Setter
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


    public SQLExecuteState(String sql, ConnectionProxy connectionProxy, Statement statement, DBMetaData metaData) {
        this.sql = sql;
        this.connectionProxy = connectionProxy;
        this.statement = statement;
        this.metaData = metaData;

        this.indexParams = new HashMap<>();
        this.mapParams = new HashMap<>();
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
        mapParams.put(key, value);
    }

    /**
     * 更新sql参数
     *
     * @param index 参数索引
     * @param value 参数值
     */
    public void setParam(int index, Object value) {
        indexParams.put(index, value);
    }

    /**
     * 获取参数列表
     * @return List
     */
    public List<Object> getListParams(){
        List<Object> list = new ArrayList<>();
        if (indexParams.isEmpty()) {
            return list;
        }
        List<Integer> keys = new ArrayList<>(indexParams.keySet());
        Collections.sort(keys);
        for(Integer key: keys){
            list.add(indexParams.get(key));
        }
        return list;
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
     * @param sql sql
     * @return 查询结果
     * @throws SQLException 查询异常
     */
    public List<Map<String, Object>> query(String sql) throws SQLException {
        return this.query(sql,new ArrayList<>());
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
        PreparedStatement preparedStatement = connectionProxy.getConnection().prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            preparedStatement.setObject(i + 1, param);
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                map.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i));
            }
            list.add(map);
        }
        resultSet.close();
        return list;
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
                        if(dbColumn!=null) {
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
     * @return jdbcKey
     */
    public String getJdbcKey(){
        if(metaData==null){
            return null;
        }
        return metaData.getKeyJdbcKey();
    }


    /**
     * 更新数据库的元数据信息
     * @param tableName 表名
     */
    public void updateMetaData(String tableName) throws SQLException {
        // 当前表需要更新时，将会连同所有带更新的表一次性全部更新
        if (this.metaData.isUpdateTableMeta(tableName)) {
            DBScanner dbScanner = new DBScanner(connectionProxy.getConnection(), getDriverProperties());
            dbScanner.updateMetadata(this.metaData);
        }
    }
}
