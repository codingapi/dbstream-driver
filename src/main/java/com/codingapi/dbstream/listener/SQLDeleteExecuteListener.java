package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.parser.DeleteDBEventParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.sqlparser.DeleteSQLParser;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.TransactionEventPools;
import com.codingapi.dbstream.utils.SQLUtils;

import java.sql.SQLException;
import java.util.List;

public class SQLDeleteExecuteListener implements SQLExecuteListener {

    private final static ThreadLocal<DeleteDBEventParser> threadLocal = new ThreadLocal<>();

    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        String sql = executeState.getSql();
        if (SQLUtils.isDeleteSQL(sql)) {
            try {
                threadLocal.remove();
                DeleteSQLParser sqlParser = new DeleteSQLParser(sql);
                String tableName = sqlParser.getTableName();
                executeState.updateMetaData(tableName);
                DbTable dbTable = executeState.getDbTable(tableName);
                if (dbTable != null && DBStreamContext.getInstance().support(executeState.getDriverProperties(), dbTable)) {
                    DeleteDBEventParser dataParser = new DeleteDBEventParser(executeState, sqlParser, dbTable);
                    dataParser.prepare();
                    threadLocal.set(dataParser);
                }
            } catch (Exception e) {
                threadLocal.remove();
                throw new SQLException(e);
            }
        } else {
            threadLocal.remove();
        }
    }

    @Override
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        String sql = executeState.getSql();
        String transactionKey = executeState.getTransactionKey();
        if (SQLUtils.isDeleteSQL(sql)) {
            DeleteDBEventParser dataParser = threadLocal.get();
            if (dataParser != null) {
                List<DBEvent> eventList = dataParser.loadEvents(result);
                TransactionEventPools.getInstance().addEvents(transactionKey, eventList);
                threadLocal.remove();
            }
        }
    }
}
