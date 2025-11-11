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

    private final List<Map<String, Object>> prepareList = new ArrayList<>();


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
        prepareList.clear();
        prepareList.addAll(this.executeState.query(query, params));
    }

    private String loadUpdateRowSQL() {
        String whereSQL = this.sqlParser.getWhereSQL();
        String tableName = this.dbTable.getName();
        StringBuilder querySQL = new StringBuilder();
        querySQL.append("SELECT ");
        if(dbTable.hasPrimaryKeys()) {
            for (DbColumn dbColumn : dbTable.getPrimaryColumns()) {
                if (this.aliasTable != null) {
                    querySQL.append(this.aliasTable).append(".");
                }
                querySQL.append(dbColumn.getName()).append(",");
            }
            querySQL.deleteCharAt(querySQL.length() - 1);
        }else {
            querySQL.append(" * ");
        }
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
        List<Object> updateParams = this.executeState.getListParams();
        for (Map<String, Object> params : this.prepareList) {
            String jdbcUrl = this.executeState.getJdbcUrl();
            DBEvent event = new DBEvent(jdbcUrl, this.dbTable.getName(), EventType.UPDATE);
            List<String> columns = this.sqlParser.getColumnValues();
            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                Object value = updateParams.get(i);
                DbColumn dbColumn = dbTable.getColumnByName(column);
                if (dbColumn != null) {
                    event.set(dbColumn.getName(), value);
                }
            }
            for (String key : params.keySet()) {
                DbColumn dbColumn = dbTable.getColumnByName(key);
                if (dbColumn != null) {
                    event.set(dbColumn.getName(), params.get(key));
                    event.addPrimaryKey(dbColumn.getName());
                }
            }
            eventList.add(event);
        }
        return eventList;
    }
}
