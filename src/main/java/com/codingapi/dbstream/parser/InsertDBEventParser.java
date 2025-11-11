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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertDBEventParser {

    private final InsertSQLParser sqlParser;
    private final SQLExecuteState executeState;
    private final DbTable dbTable;
    private boolean defaultInsertSQL = true;
    private List<Map<String, Object>> dataList = new ArrayList<>();
    private final List<String> columns;

    public InsertDBEventParser(SQLExecuteState executeState, InsertSQLParser sqlParser, DbTable dbTable) {
        this.sqlParser = sqlParser;
        this.executeState = executeState;
        this.dbTable = dbTable;
        this.columns = sqlParser.getColumnValues();
    }

    public List<DBEvent> loadEvents(Object result) throws SQLException {
        if (ResultSetUtils.isNotUpdatedRows(result)) {
            return new ArrayList<>();
        }
        return this.loadDataEvents();
    }


    private List<DBEvent> loadDataEvents() {
        String jdbcUrl = this.executeState.getJdbcUrl();
        List<DBEvent> eventList = new ArrayList<>();
        List<Map<String, Object>> generateValues = this.executeState.getStatementGenerateKeys(dbTable);

        // 插入拦截到的数据
        for (Map<String, Object> data : dataList) {
            DBEvent event = new DBEvent(jdbcUrl, this.dbTable.getName(), EventType.INSERT);
            for (String key : data.keySet()) {
                DbColumn dbColumn = dbTable.getColumnByName(key);
                if (dbColumn != null) {
                    event.set(dbColumn.getName(), data.get(key));
                    if (dbColumn.isPrimaryKey()) {
                        event.addPrimaryKey(dbColumn.getName());
                    }
                }
            }
            eventList.add(event);
        }

        // 放入自动生成的数据
        if(!generateValues.isEmpty()) {
            for(int i=0;i<generateValues.size();i++){
                Map<String, Object> generateValue = generateValues.get(i);
                DBEvent event  = eventList.get(i);
                for (String key : generateValue.keySet()) {
                    DbColumn dbColumn = dbTable.getColumnByName(key);
                    if (dbColumn != null) {
                        event.set(dbColumn.getName(), generateValue.get(key));
                        if (dbColumn.isPrimaryKey()) {
                            event.addPrimaryKey(dbColumn.getName());
                        }
                    }
                }
            }
        }

        return eventList;
    }

    private boolean isColumnPrimaryKey(String primaryKey) {
        for (String column : columns) {
            if (primaryKey.equalsIgnoreCase(column)) {
                return true;
            }
        }
        return false;
    }


    private boolean columnsHasPrimaryKeys() {
        List<String> primaryKeys = this.dbTable.getPrimaryKeys();
        for (String primaryKey : primaryKeys) {
            if (!this.isColumnPrimaryKey(primaryKey)) {
                return false;
            }
        }
        return true;
    }


    public void prepare() throws SQLException {
        this.defaultInsertSQL = this.sqlParser.isDefaultInsertSQL();
        if (this.defaultInsertSQL) {
            this.loadDefaultInsertDataList();
        } else {
            this.loadSelectInsertDataList();
        }
    }

    private void loadSelectInsertDataList() throws SQLException {
        String query = this.sqlParser.getValuesSQL();
        int paramCount = SQLUtils.paramsCount(query);
        List<Object> listParams = this.executeState.getListParams();
        List<Object> queryParams = new ArrayList<>();
        for (int i = 0; i < listParams.size(); i++) {
            if (i + 1 >= paramCount) {
                queryParams.add(listParams.get(i));
            }
        }
        this.dataList = this.executeState.query(query, queryParams);
    }

    private void loadDefaultInsertDataList() throws SQLException {
        List<InsertSQLParser.InsertValue> values = this.sqlParser.getValues();
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            InsertSQLParser.InsertValue insertValue = values.get(i);
            Object value = insertValue;

            if (insertValue.isSelect()) {
                List<Map<String, Object>> columValues = this.executeState.query(insertValue.getValue());
                if (!columValues.isEmpty()) {
                    Map<String, Object> first = columValues.get(0);
                    for (String key : first.keySet()) {
                        value = first.get(key);
                    }
                }
            } else if (insertValue.isJdbc()) {
                value = i + 1;
            } else {
                value = insertValue.getValue();
            }
            data.put(column, value);
        }
        dataList.add(data);
    }

}
