package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.stream.DBEvent;

import java.sql.SQLException;
import java.util.List;

public interface DataParser {

    List<DBEvent> loadEvents(Object result) throws SQLException;

    void prepare() throws SQLException;
}
