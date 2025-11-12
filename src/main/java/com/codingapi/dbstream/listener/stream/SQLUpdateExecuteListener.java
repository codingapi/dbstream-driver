package com.codingapi.dbstream.listener.stream;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.parser.UpdateDBEventParser;
import com.codingapi.dbstream.parser.UpdateSQLParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.utils.SQLUtils;

public class SQLUpdateExecuteListener extends SQLStreamExecuteListener {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean isSupport(String sql) {
        return SQLUtils.isUpdateSQL(sql);
    }

    @Override
    public SQLParser createSQLParser(String sql) {
        return new UpdateSQLParser(sql);
    }

    @Override
    public DBEventParser createDbEventParser(SQLExecuteState sqlExecuteState, SQLParser sqlParser, DbTable dbTable) {
        return new UpdateDBEventParser(sqlExecuteState, (UpdateSQLParser) sqlParser, dbTable);
    }

}
