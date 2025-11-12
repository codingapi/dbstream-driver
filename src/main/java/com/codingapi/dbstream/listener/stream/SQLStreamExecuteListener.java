package com.codingapi.dbstream.listener.stream;

import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import com.codingapi.dbstream.parser.*;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.TransactionEventPools;

import java.sql.SQLException;
import java.util.List;

public abstract class SQLStreamExecuteListener implements SQLExecuteListener {

    public abstract boolean isSupport(String sql);

    public abstract SQLParser createSQLParser(String sql);

    public abstract DBEventParser createDbEventParser(SQLExecuteState sqlExecuteState,SQLParser sqlParser,DbTable dbTable);

    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        String sql = executeState.getSql();
        if (this.isSupport(sql)) {
            try {
                ThreadLocalContext.getInstance().remove();
                SQLParser sqlParser = this.createSQLParser(sql);
                String tableName = sqlParser.getTableName();
                executeState.updateMetaData(tableName);
                DbTable dbTable = executeState.getDbTable(tableName);
                if (dbTable != null && DBStreamContext.getInstance().support(executeState.getDriverProperties(), dbTable)) {
                    if (executeState.isBatchMode()) {
                        List<SQLExecuteState> executeStateList = executeState.getBatchSQLExecuteStateList();
                        for (int i = 0; i < executeStateList.size(); i++) {
                            SQLExecuteState sqlExecuteState = executeStateList.get(i);
                            DBEventParser dataParser = this.createDbEventParser(sqlExecuteState,sqlParser,dbTable);
                            dataParser.prepare();
                            ThreadLocalContext.getInstance().push(i, dataParser);
                        }
                    } else {
                        DBEventParser dataParser = this.createDbEventParser(executeState,sqlParser,dbTable);
                        dataParser.prepare();
                        ThreadLocalContext.getInstance().push(dataParser);
                    }
                }
            } catch (Exception e) {
                ThreadLocalContext.getInstance().remove();
                throw new SQLException(e);
            }
        }
    }

    @Override
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        String sql = executeState.getSql();
        String transactionKey = executeState.getTransactionKey();
        if (this.isSupport(sql)) {
            if (executeState.isBatchMode()) {
                List<SQLExecuteState> executeStateList = executeState.getBatchSQLExecuteStateList();
                for (int i = 0; i < executeStateList.size(); i++) {
                    DBEventParser dataParser = ThreadLocalContext.getInstance().get(i);
                    if (dataParser != null) {
                        List<DBEvent> eventList = dataParser.loadEvents(result);
                        TransactionEventPools.getInstance().addEvents(transactionKey, eventList);
                    }
                }
                ThreadLocalContext.getInstance().remove();
            } else {
                DBEventParser dataParser = ThreadLocalContext.getInstance().get();
                if (dataParser != null) {
                    List<DBEvent> eventList = dataParser.loadEvents(result);
                    TransactionEventPools.getInstance().addEvents(transactionKey, eventList);
                }
                ThreadLocalContext.getInstance().remove();
            }
        }
    }

}
