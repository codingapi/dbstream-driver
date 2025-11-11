package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.sqlparser.InsertSQLParser;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.EventType;
import com.codingapi.dbstream.utils.ResultSetUtils;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InsertDBEventParser {

    private final InsertSQLParser sqlParser;
    private final SQLExecuteState executeState;
    private final DbTable dbTable;

    private final List<Map<String, Object>> prepareList = new ArrayList<>();

    public InsertDBEventParser(SQLExecuteState executeState, InsertSQLParser sqlParser, DbTable dbTable) {
        this.sqlParser = sqlParser;
        this.executeState = executeState;
        this.dbTable = dbTable;
    }

    public List<DBEvent> loadEvents(Object result) throws SQLException {
        List<DBEvent> eventList = new ArrayList<>();
        if (ResultSetUtils.isNotUpdatedRows(result)) {
            return eventList;
        }
        if (this.prepareList.isEmpty()) {
            eventList.addAll(this.loadDefaultInsertEvent());
        } else {
            System.err.println("INSERT INTO SELECT MODE CAN'T SUPPORT MULTIPLE EVENTS.");
            System.err.println("<-----------WARNING-------------->");
            System.err.println("SAVE DATA:" + this.prepareList);
            System.err.println(">--------------------------------<");
        }
        return eventList;
    }


    private void loadSelectPrepare() throws SQLException {
        String query = sqlParser.getSelectSQL();
        List<Object> params = this.loadUpdateRowParamList();
        this.prepareList.clear();
        this.prepareList.addAll(this.executeState.query(query, params));
    }

    private List<Object> loadUpdateRowParamList() {
        List<Object> params = new ArrayList<>();
        String nativeSQL = this.executeState.getSql();

        int whereIndex = nativeSQL.toUpperCase().indexOf(" SELECT ");
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


    private List<DBEvent> loadDefaultInsertEvent() {
        String jdbcUrl = this.executeState.getJdbcUrl();
        List<DBEvent> eventList = new ArrayList<>();
        List<Map<String, Object>> primaryKeyValues = this.executeState.getStatementGenerateKeys(dbTable);
        for (Map<String, Object> map : primaryKeyValues) {
            DBEvent event = new DBEvent(jdbcUrl, this.dbTable.getName(), EventType.INSERT);
            List<Object> params = this.executeState.getListParams();
            List<String> insertColumns = this.sqlParser.getColumnValues();
            //主键
            for (String key : map.keySet()) {
                DbColumn dbColumn = dbTable.getColumnByName(key);
                if (dbColumn.isPrimaryKey()) {
                    event.addPrimaryKey(dbColumn.getName());
                }
                event.set(dbColumn.getName(), map.get(key));
            }

            //字段
            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);
                String column = insertColumns.get(i);
                DbColumn dbColumn = dbTable.getColumnByName(column);
                if (dbColumn != null && !dbColumn.isPrimaryKey()) {
                    event.set(dbColumn.getName(), value);
                }
            }
            eventList.add(event);
        }
        return eventList;
    }




    public void prepare() throws SQLException {
        String selectSQL = this.sqlParser.getSelectSQL();
        if (selectSQL!=null) {
            this.loadSelectPrepare();
        } else {
            prepareList.clear();
        }
    }
}
