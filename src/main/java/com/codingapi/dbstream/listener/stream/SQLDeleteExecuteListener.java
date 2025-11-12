package com.codingapi.dbstream.listener.stream;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.DeleteDBEventParser;
import com.codingapi.dbstream.parser.DeleteSQLParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.utils.SQLUtils;

public class SQLDeleteExecuteListener extends SQLStreamExecuteListener {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean isSupport(String sql) {
        return SQLUtils.isDeleteSQL(sql);
    }

    @Override
    public SQLParser createSQLParser(String sql) {
        return new DeleteSQLParser(sql);
    }

    @Override
    public DBEventParser createDbEventParser(SQLExecuteState sqlExecuteState, SQLParser sqlParser, DbTable dbTable) {
        return new DeleteDBEventParser(sqlExecuteState, (DeleteSQLParser) sqlParser, dbTable);
    }

}
