package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.listener.SQLRunningState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.EventType;
import com.codingapi.dbstream.utils.ResultSetUtils;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDBEventParser implements DBEventParser {

    private final UpdateSQLParser sqlParser;
    private final SQLRunningState executeState;
    private final DbTable dbTable;

    // 执行前的数据记录信息
    private List<Map<String, Object>> prepareList = new ArrayList<>();


    public UpdateDBEventParser(SQLRunningState executeState, UpdateSQLParser sqlParser, DbTable dbTable) {
        this.executeState = executeState;
        this.sqlParser = sqlParser;
        this.dbTable = dbTable;
    }

    /**
     * 分析受影响的数据
     */
    @Override
    public void prepare() throws SQLException {
        this.updateRows();
    }

    private void updateRows() throws SQLException {
        String query = this.loadUpdateRowSQL();
        List<Object> params = this.loadUpdateRowParamList();
        try {
            prepareList = this.executeState.query(query, params);
        } catch (SQLException e) {
            // dbstream 内部 SQL 不经过业务侧的 SQL 日志，失败时必须带上原文，否则报错会归因到业务 SQL
            throw new SQLException("dbstream 前镜像查询失败, sql=" + query, e);
        }
    }

    /**
     * 组装受影响数据的查询SQL
     */
    private String loadUpdateRowSQL() {
        String aliasTable = this.sqlParser.getTableAlias();
        String whereSQL = this.sqlParser.getWhereSQL();
        String tableName = this.dbTable.getName();
        StringBuilder querySQL = new StringBuilder();
        querySQL.append("SELECT ");
        for (DbColumn dbColumn : dbTable.getPrimaryColumns()) {
            if (aliasTable != null) {
                querySQL.append(aliasTable).append(".");
            }
            querySQL.append(dbColumn.getName()).append(",");
        }
        querySQL.deleteCharAt(querySQL.length() - 1);
        querySQL.append(" FROM ").append(tableName);
        if (aliasTable != null) {
            querySQL.append(" AS ").append(aliasTable);
        }
        querySQL.append(" WHERE ");
        if (whereSQL != null) {
            querySQL.append(whereSQL);
        } else {
            querySQL.append(" 1=1 ");
        }
        return querySQL.toString();
    }

    /**
     * 组装受影响数据的查询参数
     */
    private List<Object> loadUpdateRowParamList() {
        List<Object> params = new ArrayList<>();
        String nativeSQL = this.executeState.getSql();

        int whereIndex = nativeSQL.toUpperCase().indexOf(" WHERE ");
        String beforeSQL;
        if (whereIndex > 0) {
            beforeSQL = nativeSQL.substring(0, whereIndex);
        } else {
            beforeSQL = nativeSQL;
        }

        int paramsSize = SQLUtils.jdbcParamsCount(beforeSQL);

        List<Object> paramsList = this.executeState.getListParams();
        for (int i = 0; i < paramsList.size(); i++) {
            if (i >= paramsSize) {
                params.add(paramsList.get(i));
            }
        }
        return params;
    }


    private List<Map<String, Object>> queryLatestData() throws SQLException {
        String sql = this.latestSQL();
        try {
            return this.executeState.query(sql, new ArrayList<>());
        } catch (SQLException e) {
            throw new SQLException("dbstream 后镜像查询失败, sql=" + sql, e);
        }
    }


    /**
     * 查询最新的数据状态，由于update 赋值操作存在数据库中赋值的可能，因此无法准确解析执行的结果
     */
    private String latestSQL() {
        StringBuilder querySQL = new StringBuilder();
        List<String> columns = new ArrayList<>();
        columns.addAll(this.sqlParser.getColumnValues());
        columns.addAll(this.dbTable.getPrimaryKeys());
        querySQL.append("SELECT ");
        querySQL.append(String.join(",", columns));
        querySQL.append(" FROM ").append(this.dbTable.getName());
        querySQL.append(" WHERE ");
        List<String> conditions = new ArrayList<>();
        for (String primaryKey : this.dbTable.getPrimaryKeys()) {
            List<String> params = this.getPrimaryKeyStringValue(primaryKey);
            // 主键值列表为空时拼出的 "IN ()" 是非法 SQL，直接跳过该条件
            if (params.isEmpty()) {
                continue;
            }
            conditions.add(primaryKey + " IN (" + String.join(",", params) + ")");
        }
        if (conditions.isEmpty()) {
            // 没有任何可用的主键值时返回空结果集，避免退化为全表扫描
            conditions.add("1=0");
        }
        querySQL.append(String.join(" AND ", conditions));
        return querySQL.toString();
    }


    /**
     * 提取对应主键下的值数据，拼接查询sql使用
     */
    private List<String> getPrimaryKeyStringValue(String primaryKey) {
        List<String> params = new ArrayList<>();
        for (Map<String, Object> data : this.prepareList) {
            for (String key : data.keySet()) {
                if (key.equalsIgnoreCase(primaryKey)) {
                    Object value = data.get(key);
                    // 主键理论上非空，取到 null 说明该行数据异常，跳过而不是拼出 "IN (null)"
                    if (value == null) {
                        continue;
                    }
                    if (value instanceof String) {
                        // 字符串值需转义单引号，否则值本身含引号时会拼出非法 SQL
                        params.add("'" + ((String) value).replace("'", "''") + "'");
                    } else {
                        params.add(String.valueOf(value));
                    }
                }
            }
        }
        return params;
    }


    @Override
    public List<DBEvent> loadEvents(Object result) throws SQLException {
        List<DBEvent> eventList = new ArrayList<>();
        // 数据库执行没有受影响的行数，则直接返回空对象
        if (ResultSetUtils.isNotUpdatedRows(result)) {
            return eventList;
        }
        String jdbcUrl = this.executeState.getJdbcUrl();
        String jdbcKey = this.executeState.getJdbcKey();
        // 前镜像为空说明没有可按主键关联的行，无法定位后镜像，直接返回空事件
        if (this.prepareList.isEmpty()) {
            return eventList;
        }
        // 根据id查询最新的数据
        List<Map<String, Object>> latestData = this.queryLatestData();
        for (Map<String, Object> params : latestData) {
            DBEvent event = new DBEvent(jdbcUrl, jdbcKey, this.dbTable.getName(), EventType.UPDATE);
            for (String column : params.keySet()) {
                DbColumn dbColumn = this.dbTable.getColumnByName(column);
                if (dbColumn != null) {
                    event.set(dbColumn.getName(), params.get(column));
                    if (dbColumn.isPrimaryKey()) {
                        event.addPrimaryKey(dbColumn.getName());
                    }
                }
            }
            eventList.add(event);
        }

        return eventList;
    }
}
