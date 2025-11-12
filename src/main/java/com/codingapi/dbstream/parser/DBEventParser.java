package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.stream.DBEvent;

import java.sql.SQLException;
import java.util.List;

public interface DBEventParser {

    void prepare() throws SQLException;

    List<DBEvent> loadEvents(Object result) throws SQLException;
}
