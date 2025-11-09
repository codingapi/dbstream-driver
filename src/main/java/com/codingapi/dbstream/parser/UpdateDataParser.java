package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.EventType;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDataParser implements DataParser {

    private final SQLExecuteState executeState;
    private final Update update;
    private final Table table;
    private final String aliasTable;

    private final List<Map<String, Object>> prepareList = new ArrayList<>();

    public UpdateDataParser(SQLExecuteState executeState, Update update) {
        this.executeState = executeState;
        this.update = update;
        this.table = this.update.getTable();
        Alias alias = this.table.getAlias();
        if (alias != null) {
            this.aliasTable = alias.getName();
        } else {
            this.aliasTable = null;
        }
    }


    @Override
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
        Expression expression = this.update.getWhere();
        String tableName = this.table.getName();
        DbTable dbTable = this.executeState.getTable(tableName);
        StringBuilder querySQL = new StringBuilder();
        querySQL.append("SELECT ");
        if (dbTable != null) {
            for (DbColumn dbColumn : dbTable.getPrimaryColumns()) {
                if (this.aliasTable != null) {
                    querySQL.append(this.aliasTable).append(".");
                }
                querySQL.append(dbColumn.getName()).append(",");
            }
        }
        querySQL.deleteCharAt(querySQL.length() - 1);
        querySQL.append(" FROM ").append(tableName);
        if (this.aliasTable != null) {
            querySQL.append(" AS ").append(aliasTable);
        }
        querySQL.append(" WHERE ");
        if (expression != null) {
            querySQL.append(expression);
        } else {
            querySQL.append(" 1=1 ");
        }
        return querySQL.toString();
    }


    private List<Object> loadUpdateRowParamList() {
        List<Object> params = new ArrayList<>();
        String nativeSQL = this.executeState.getSql();

        int whereIndex = nativeSQL.toLowerCase().indexOf(" where ");
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


    @Override
    public List<DBEvent> loadEvents(Object result) throws SQLException {
        List<DBEvent> eventList = new ArrayList<>();
        if (!SQLParamUtils.isUpdateRow(result)) {
            return eventList;
        }
        List<Object> updateParams = this.executeState.getListParams();
        DbTable dbTable = this.executeState.getTable(table.getName());
        if (dbTable != null) {
            for (Map<String, Object> params : this.prepareList) {
                String jdbcUrl = this.executeState.getJdbcUrl();
                DBEvent event = new DBEvent(jdbcUrl, this.table.getName(), EventType.UPDATE);
                List<UpdateSet> updateSets = this.update.getUpdateSets();
                for (int i = 0; i < updateSets.size(); i++) {
                    UpdateSet updateSet = updateSets.get(i);
                    Object value = updateParams.get(i);
                    updateSet.getColumns().forEach(column -> {
                        String columnName = column.getColumnName();
                        DbColumn dbColumn = dbTable.getColumnByName(columnName);
                        if (dbColumn != null) {
                            event.set(dbColumn.getName(), value);
                        }
                    });
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
        }
        return eventList;
    }
}
