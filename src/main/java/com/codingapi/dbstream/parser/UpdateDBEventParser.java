package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.EventType;
import com.codingapi.dbstream.utils.ResultSetUtils;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDBEventParser implements DBEventParser{

    private final UpdateSQLParser sqlParser;
    private final SQLExecuteState executeState;
    private final DbTable dbTable;

    // 执行前的数据记录信息
    private List<Map<String, Object>> prepareList = new ArrayList<>();


    public UpdateDBEventParser(SQLExecuteState executeState, UpdateSQLParser sqlParser, DbTable dbTable) {
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
        prepareList = this.executeState.query(query, params);
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

        int paramsSize = SQLUtils.paramsCount(beforeSQL);

        List<Object> paramsList = this.executeState.getListParams();
        for (int i = 0; i < paramsList.size(); i++) {
            if (i >= paramsSize) {
                params.add(paramsList.get(i));
            }
        }
        return params;
    }


    private List<Map<String,Object>> queryLatestData() throws SQLException{
        String sql = this.latestSQL();
        return this.executeState.query(sql,new ArrayList<>());
    }


    /**
     * 查询最新的数据状态，由于update 赋值操作存在数据库中赋值的可能，因此无法准确解析执行的结果
     */
    private String latestSQL(){
        StringBuilder querySQL = new StringBuilder();
        List<String> columns = new ArrayList<>();
        columns.addAll(this.sqlParser.getColumnValues());
        columns.addAll(this.dbTable.getPrimaryKeys());
        querySQL.append("SELECT ");
        querySQL.append(String.join(",",columns));
        querySQL.append(" FROM ").append(this.dbTable.getName());
        querySQL.append(" WHERE ");
        for(String primaryKey:this.dbTable.getPrimaryKeys()) {
            querySQL.append(" ").append(primaryKey);
            querySQL.append(" IN (");
            List<String> params = this.getPrimaryKeyStringValue(primaryKey);
            querySQL.append(String.join(",", params));
            querySQL.append(")");
            querySQL.append(" AND ");
        }
        querySQL.append(" 1=1 ");
        return querySQL.toString();
    }


    /**
     * 提取对应主键下的值数据，拼接查询sql使用
     */
    private List<String> getPrimaryKeyStringValue(String primaryKey){
        List<String> params = new ArrayList<>();
        for(Map<String,Object> data:this.prepareList){
            for(String key:data.keySet()){
                if(key.equalsIgnoreCase(primaryKey)){
                    Object value = data.get(key);
                    if(value instanceof String){
                        params.add(String.format("'%s'", value));
                    }else {
                        params.add(data.get(key).toString());
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
        // 根据id查询最新的数据
        List<Map<String,Object>> latestData = this.queryLatestData();
        for (Map<String, Object> params : latestData) {
            DBEvent event = new DBEvent(jdbcUrl,jdbcKey, this.dbTable.getName(), EventType.UPDATE);
            for(String column:params.keySet()){
                DbColumn dbColumn = this.dbTable.getColumnByName(column);
                if(dbColumn!=null){
                    event.set(dbColumn.getName(),params.get(column));
                    if(dbColumn.isPrimaryKey()){
                        event.addPrimaryKey(dbColumn.getName());
                    }
                }
            }
            eventList.add(event);
        }

        return eventList;
    }
}
