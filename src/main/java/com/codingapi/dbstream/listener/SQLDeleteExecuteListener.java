package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.parser.DeleteDataParser;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.TransactionEventPools;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class SQLDeleteExecuteListener implements SQLExecuteListener {

    private final static ThreadLocal<DeleteDataParser> threadLocal = new ThreadLocal<>();

    private static final Pattern DELETE_SQL_PATTERN = Pattern.compile(
            "^\\s*(?i)(DELETE)\\b.*",
            Pattern.DOTALL
    );


    public static boolean match(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return DELETE_SQL_PATTERN.matcher(sql.trim()).matches();
    }

    @Override
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        String sql = executeState.getSql();
        String transactionKey = executeState.getTransactionKey();
        if (match(sql) && executeState.hasMetaData()) {
            DeleteDataParser dataParser = threadLocal.get();
            if (dataParser != null) {
                List<DBEvent> eventList = dataParser.loadEvents(result);
                TransactionEventPools.getInstance().addEvents(transactionKey,eventList);
                threadLocal.remove();
            }
        }
    }

    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        String sql = executeState.getSql();
        if (match(sql) && executeState.hasMetaData()) {
            try {
                threadLocal.remove();
                Statement parserStatement = CCJSqlParserUtil.parse(sql);
                Delete delete = (Delete) parserStatement;
                DeleteDataParser dataParser = new DeleteDataParser(executeState, delete);
                dataParser.prepare();
                threadLocal.set(dataParser);
            } catch (Exception e) {
                threadLocal.remove();
                throw new SQLException(e);
            }
        } else {
            threadLocal.remove();
        }
    }
}
