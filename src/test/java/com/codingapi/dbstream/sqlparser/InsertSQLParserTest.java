package com.codingapi.dbstream.sqlparser;

import com.codingapi.dbstream.parser.InsertSQLParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class InsertSQLParserTest {

    @ParameterizedTest
    @CsvFileSource(resources = "insert.sql.csv", delimiterString = "|", numLinesToSkip = 1)
    void insert(String sql, String table, String columns, String values) {
        InsertSQLParser sqlParser = new InsertSQLParser(sql);
        assertEquals(sqlParser.getTableName(), table);
        assertEquals(sqlParser.getValuesSQL(), values);
        assertEquals(sqlParser.getColumnValues(), Arrays.asList(columns.split(",")));
    }
}