package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.sqlparser.DeleteSQLParser;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.EventType;
import com.codingapi.dbstream.utils.ResultSetUtils;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteDBEventParser   {

    private final List<Map<String, Object>> prepareList = new ArrayList<>();

    private final String aliasTable;
    private final DeleteSQLParser sqlParser;
    private final SQLExecuteState executeState;
    private final DbTable dbTable;

    public DeleteDBEventParser(SQLExecuteState executeState, DeleteSQLParser sqlParser, DbTable dbTable) {
        this.sqlParser = sqlParser;
        this.dbTable = dbTable;
        this.executeState = executeState;
        this.aliasTable = sqlParser.getTableAlias();
    }

    public void prepare() throws SQLException {
        this.updateRows();
    }

    private void updateRows() throws SQLException {
        String query = this.loadUpdateRowSQL();
        List<Object> params = this.loadUpdateRowParamList();
        prepareList.clear();
        prepareList.addAll(this.executeState.query(query, params));
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


    public List<DBEvent> loadEvents(Object result) throws SQLException {
        List<DBEvent> eventList = new ArrayList<>();
        if (ResultSetUtils.isNotUpdatedRows(result)) {
            return eventList;
        }
        String jdbcUrl = this.executeState.getJdbcUrl();
        String jdbcKey = this.executeState.getJdbcKey();

        for (Map<String, Object> params : this.prepareList) {
            DBEvent event = new DBEvent(jdbcUrl,jdbcKey, this.dbTable.getName(), EventType.DELETE);
            for (String key : params.keySet()) {
                DbColumn dbColumn = dbTable.getColumnByName(key);
                if (dbColumn != null) {
                    event.set(dbColumn.getName(), params.get(key));
                    if(dbColumn.isPrimaryKey()) {
                        event.addPrimaryKey(dbColumn.getName());
                    }
                }
            }
            eventList.add(event);
        }
        return eventList;
    }
}
