package com.codingapi.dbstream.sqlparser;

import com.codingapi.dbstream.parser.UpdateSQLParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UpdateSQLParserTest {

    @ParameterizedTest
    @CsvFileSource(resources = "update.sql.csv",delimiterString = "|",numLinesToSkip = 1)
    void update(String sql,String table,String alias,String columns,String where){
        UpdateSQLParser sqlParser = new UpdateSQLParser(sql);
        assertEquals(sqlParser.getTableName(),table);
        assertEquals(sqlParser.getTableAlias(),alias);
        assertEquals(sqlParser.getColumnValues(), Arrays.asList(columns.split(",")));
        assertEquals(sqlParser.getWhereSQL(),where);

    }
}