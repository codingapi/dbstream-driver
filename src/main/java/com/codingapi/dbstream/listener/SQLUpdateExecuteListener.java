package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.parser.UpdateDBEventParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.TransactionEventPools;
import com.codingapi.dbstream.utils.SQLParamUtils;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;

import java.sql.SQLException;
import java.util.List;

public class SQLUpdateExecuteListener implements SQLExecuteListener {

    private final static ThreadLocal<UpdateDBEventParser> threadLocal = new ThreadLocal<>();

    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        String sql = executeState.getSql();
        if (SQLParamUtils.isUpdateSQL(sql)) {
            try {
                threadLocal.remove();
                Statement parserStatement = CCJSqlParserUtil.parse(sql);
                Update update = (Update) parserStatement;
                Table table = update.getTable();
                DbTable dbTable = executeState.getDbTable(table.getName());
                if (dbTable != null && DBStreamContext.getInstance().support(executeState.getDriverProperties(), dbTable)) {
                    UpdateDBEventParser dataParser = new UpdateDBEventParser(executeState, update, table, dbTable);
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
        if (SQLParamUtils.isUpdateSQL(sql)) {
            UpdateDBEventParser dataParser = threadLocal.get();
            if (dataParser != null) {
                List<DBEvent> eventList = dataParser.loadEvents(result);
                TransactionEventPools.getInstance().addEvents(transactionKey, eventList);
                threadLocal.remove();
            }
        }
    }
}
