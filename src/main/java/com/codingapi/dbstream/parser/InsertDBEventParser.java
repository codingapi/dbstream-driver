package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.EventType;
import com.codingapi.dbstream.utils.ResultSetUtils;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertDBEventParser implements DBEventParser {

    private final InsertSQLParser sqlParser;
    private final SQLExecuteState executeState;
    private final DbTable dbTable;

    // 插入的数据记录
    private List<Map<String, Object>> dataList = new ArrayList<>();
    // 插入的字段信息
    private final List<String> columns;

    public InsertDBEventParser(SQLExecuteState executeState, InsertSQLParser sqlParser, DbTable dbTable) {
        this.sqlParser = sqlParser;
        this.executeState = executeState;
        this.dbTable = dbTable;
        this.columns = sqlParser.getColumnValues();
    }

    @Override
    public List<DBEvent> loadEvents(Object result) throws SQLException {
        // 数据库执行没有受影响的行数，则直接返回空对象
        if (ResultSetUtils.isNotUpdatedRows(result)) {
            return new ArrayList<>();
        }
        return this.loadDataEvents();
    }


    private List<DBEvent> loadDataEvents() {
        String jdbcUrl = this.executeState.getJdbcUrl();
        String jdbcKey = this.executeState.getJdbcKey();
        List<DBEvent> eventList = new ArrayList<>();
        List<Map<String, Object>> generateValues = this.executeState.getStatementGenerateKeys(dbTable);

        // 插入拦截到的数据
        for (Map<String, Object> data : dataList) {
            DBEvent event = new DBEvent(jdbcUrl, jdbcKey, this.dbTable.getName(), EventType.INSERT);
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
        if (!generateValues.isEmpty()) {
            for (int i = 0; i < generateValues.size(); i++) {
                Map<String, Object> generateValue = generateValues.get(i);
                DBEvent event = eventList.get(i);
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

    /**
     * 分析插入的数据信息
     */
    @Override
    public void prepare() throws SQLException {
        // 是否为默认的insert语句，即非insert into select 语句
        boolean defaultInsertSQL = this.sqlParser.isDefaultInsertSQL();
        if (defaultInsertSQL) {
            this.loadDefaultInsertDataList();
        } else {
            this.loadSelectInsertDataList();
        }
    }

    private void loadSelectInsertDataList() throws SQLException {
        String query = this.sqlParser.getValuesSQL();
        int paramCount = SQLUtils.jdbcParamsCount(query);
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
        List<Object> paramList = this.executeState.getListParams();
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            InsertSQLParser.InsertValue insertValue = values.get(i);
            Object value = insertValue;

            // 根据不同的参数类型，获取对应的真实参数数据
            if (insertValue.isSelect()) {
                List<Map<String, Object>> columValues = this.executeState.query(insertValue.getValue());
                if (!columValues.isEmpty()) {
                    Map<String, Object> first = columValues.get(0);
                    for (String key : first.keySet()) {
                        value = first.get(key);
                    }
                }
            } else if (insertValue.isJdbc()) {
                value = paramList.get(insertValue.getJdbcParamIndex()-1);
            } else {
                value = insertValue.getValue();
            }
            data.put(column, value);
        }
        dataList.add(data);
    }

}
