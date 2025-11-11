package com.codingapi.dbstream.sqlparser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteSQLParserTest {


    @ParameterizedTest
    @CsvFileSource(resources = "delete.sql.csv",numLinesToSkip = 1,delimiterString = "|")
    void delete(String sql,String table,String alias,String where){
        DeleteSQLParser p = new DeleteSQLParser(sql);
        assertEquals(p.getWhereSQL(),where);
        assertEquals(p.getTableName(),table);
        assertEquals(p.getTableAlias(),alias);
    }
}