package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.EventType;
import com.codingapi.dbstream.utils.SQLParamUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.Values;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InsertDBEventParser extends DBEventParser {

    private final Insert insert;

    private final List<Map<String, Object>> prepareList = new ArrayList<>();

    public InsertDBEventParser(SQLExecuteState executeState, Statement statement, Table table, DbTable dbTable) {
        super(executeState, statement, table, dbTable);
        this.insert = (Insert) statement;
    }

    @Override
    public List<DBEvent> loadEvents(Object result) throws SQLException {
        List<DBEvent> eventList = new ArrayList<>();
        if (SQLParamUtils.isNotUpdatedRows(result)) {
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


    private void loadSelectPrepare(PlainSelect select) throws SQLException {
        String query = select.toString();
        List<Object> params = this.loadUpdateRowParamList();
        this.prepareList.clear();
        this.prepareList.addAll(this.executeState.query(query, params));
    }

    private List<Object> loadUpdateRowParamList() {
        List<Object> params = new ArrayList<>();
        String nativeSQL = this.executeState.getSql();

        int whereIndex = nativeSQL.toLowerCase().indexOf(" select ");
        String beforeSQL;
        if (whereIndex > 0) {
            beforeSQL = nativeSQL.substring(0, whereIndex);
        } else {
            beforeSQL = nativeSQL;
        }

        int paramsSize = SQLParamUtils.paramsCount(beforeSQL);

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
            List<String> insertColumns = this.loadInsertColumns();
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


    private List<String> loadInsertColumns() {
        ExpressionList<?> values =  this.insert.getValues().getExpressions();
        ExpressionList<Column> columns = this.insert.getColumns();
        List<String> columnList = new ArrayList<>();
        for (int i=0;i<columns.size();i++) {
            Column column = columns.get(i);
            Expression expression = values.get(i);
            if(expression instanceof JdbcParameter) {
                columnList.add(column.getColumnName());
            }
        }
        return columnList;
    }


    @Override
    public void prepare() throws SQLException {
        Select select = this.insert.getSelect();
        if (select instanceof PlainSelect) {
            this.loadSelectPrepare((PlainSelect) select);
        } else {
            prepareList.clear();
        }
    }
}
