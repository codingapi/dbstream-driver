package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.sqlparser.UpdateSQLParser;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.EventType;
import com.codingapi.dbstream.utils.ResultSetUtils;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDBEventParser {

    private final UpdateSQLParser sqlParser;
    private final String aliasTable;
    private final SQLExecuteState executeState;
    private final DbTable dbTable;

    private List<Map<String, Object>> prepareList = new ArrayList<>();


    public UpdateDBEventParser(SQLExecuteState executeState, UpdateSQLParser sqlParser, DbTable dbTable) {
        this.executeState = executeState;
        this.sqlParser = sqlParser;
        this.dbTable = dbTable;
        this.aliasTable = sqlParser.getTableAlias();
    }

    public void prepare() throws SQLException {
        this.updateRows();
    }

    private void updateRows() throws SQLException {
        String query = this.loadUpdateRowSQL();
        List<Object> params = this.loadUpdateRowParamList();
        prepareList = this.executeState.query(query, params);
    }

    private String loadUpdateRowSQL() {
        String whereSQL = this.sqlParser.getWhereSQL();
        String tableName = this.dbTable.getName();
        StringBuilder querySQL = new StringBuilder();
        querySQL.append("SELECT ");
        for (DbColumn dbColumn : dbTable.getPrimaryColumns()) {
            if (this.aliasTable != null) {
                querySQL.append(this.aliasTable).append(".");
            }
            querySQL.append(dbColumn.getName()).append(",");
        }
        querySQL.deleteCharAt(querySQL.length() - 1);
        querySQL.append(" FROM ").append(tableName);
        if (this.aliasTable != null) {
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


    public List<DBEvent> loadEvents(Object result) throws SQLException {
        List<DBEvent> eventList = new ArrayList<>();
        if (ResultSetUtils.isNotUpdatedRows(result)) {
            return eventList;
        }
        // 根据id查询最新的数据
        List<Map<String,Object>> latestData = this.queryLatestData();
        for (Map<String, Object> params : latestData) {
            String jdbcUrl = this.executeState.getJdbcUrl();
            DBEvent event = new DBEvent(jdbcUrl, this.dbTable.getName(), EventType.UPDATE);
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
