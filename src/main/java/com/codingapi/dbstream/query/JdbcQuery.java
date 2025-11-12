package com.codingapi.dbstream.query;

import com.codingapi.dbstream.proxy.ConnectionProxy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC数据查询
 */
public class JdbcQuery {

    private final ConnectionProxy connection;

    public JdbcQuery(ConnectionProxy connection) {
        this.connection = connection;
    }

    /**
     * 查询
     *
     * @param sql sql
     * @return 查询结果
     * @throws SQLException 查询异常
     */
    public List<Map<String, Object>> query(String sql) throws SQLException {
        return this.query(sql, new ArrayList<>());
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
        PreparedStatement preparedStatement = connection.getConnection().prepareStatement(sql);
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



}
