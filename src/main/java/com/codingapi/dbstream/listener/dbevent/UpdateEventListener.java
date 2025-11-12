package com.codingapi.dbstream.listener.dbevent;

import com.codingapi.dbstream.listener.SQLRunningState;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.parser.UpdateDBEventParser;
import com.codingapi.dbstream.parser.UpdateSQLParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.utils.SQLUtils;

public class UpdateEventListener extends DBEventListener {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean support(String sql) {
        return SQLUtils.isUpdateSQL(sql);
    }

    @Override
    public SQLParser createSQLParser(String sql) {
        return new UpdateSQLParser(sql);
    }

    @Override
    public DBEventParser createDbEventParser(SQLRunningState runningState, SQLParser sqlParser, DbTable dbTable) {
        return new UpdateDBEventParser(runningState, (UpdateSQLParser) sqlParser, dbTable);
    }

}
