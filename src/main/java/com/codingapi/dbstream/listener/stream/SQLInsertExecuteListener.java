package com.codingapi.dbstream.listener.stream;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.InsertDBEventParser;
import com.codingapi.dbstream.parser.InsertSQLParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.utils.SQLUtils;

public class SQLInsertExecuteListener extends DBEventExecuteListener {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean support(String sql) {
        return SQLUtils.isInsertSQL(sql);
    }

    @Override
    public SQLParser createSQLParser(String sql) {
        return new InsertSQLParser(sql);
    }

    @Override
    public DBEventParser createDbEventParser(SQLExecuteState sqlExecuteState, SQLParser sqlParser, DbTable dbTable) {
        return new InsertDBEventParser(sqlExecuteState, (InsertSQLParser) sqlParser, dbTable);
    }


}
