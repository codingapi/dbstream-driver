package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

import java.sql.SQLException;
import java.util.List;

public abstract class DBEventParser {

    protected final SQLExecuteState executeState;
    protected final Statement statement;
    protected final Table table;
    protected final DbTable dbTable;

    public DBEventParser(SQLExecuteState executeState, Statement statement, Table table, DbTable dbTable) {
        this.executeState = executeState;
        this.statement = statement;
        this.table = table;
        this.dbTable = dbTable;
    }


    public abstract void prepare() throws SQLException;

    public abstract List<DBEvent> loadEvents(Object result) throws SQLException;


}
