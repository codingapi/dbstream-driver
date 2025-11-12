package com.codingapi.dbstream.listener.dbevent;

import com.codingapi.dbstream.listener.SQLRunningState;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.DeleteDBEventParser;
import com.codingapi.dbstream.parser.DeleteSQLParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.utils.SQLUtils;

public class DeleteEventListener extends DBEventListener {

    @Override
    public int order() {
        return 100;
    }

    @Override
    public boolean support(String sql) {
        return SQLUtils.isDeleteSQL(sql);
    }

    @Override
    public SQLParser createSQLParser(String sql) {
        return new DeleteSQLParser(sql);
    }

    @Override
    public DBEventParser createDbEventParser(SQLRunningState runningState, SQLParser sqlParser, DbTable dbTable) {
        return new DeleteDBEventParser(runningState, (DeleteSQLParser) sqlParser, dbTable);
    }

}
