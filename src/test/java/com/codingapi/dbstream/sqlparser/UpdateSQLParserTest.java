package com.codingapi.dbstream.sqlparser;

import com.codingapi.dbstream.parser.UpdateSQLParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class UpdateSQLParserTest {

    @ParameterizedTest
    @CsvFileSource(resources = "update.sql.csv", delimiterString = "|", numLinesToSkip = 1)
    void update(String sql, String table, String alias, String columns, String where) {
        UpdateSQLParser sqlParser = new UpdateSQLParser(sql);
        assertEquals(sqlParser.getTableName(), table);
        assertEquals(sqlParser.getTableAlias(), alias);
        assertEquals(sqlParser.getColumnValues(), Arrays.asList(columns.split(",")));
        assertEquals(sqlParser.getWhereSQL(), where);

    }

    /**
     * issue #9：SET 子句含函数调用时，函数参数里的逗号被误判为列分隔符，
     * 解析出 "?"、"?)" 等伪列名，最终拼出非法 SQL。
     */
    @Test
    void setClauseWithFunctionCall() {
        UpdateSQLParser sqlParser = new UpdateSQLParser(
                "update biz_pbm_organization set short_name_tair=replace(short_name_tair,?,?) " +
                        "where system_code like (?||'%') and short_name_tair like (?||'%') and id<>? and sys_deleted=0");

        assertEquals(sqlParser.getTableName(), "biz_pbm_organization");
        assertEquals(sqlParser.getColumnValues(), Collections.singletonList("short_name_tair"));
        assertEquals(sqlParser.getWhereSQL(),
                "system_code like (?||'%') and short_name_tair like (?||'%') and id<>? and sys_deleted=0");
    }

    /**
     * 无等号的片段（SET 项解析异常）不应被当成列名
     */
    @Test
    void setClauseWithoutAssignShouldBeIgnored() {
        UpdateSQLParser sqlParser = new UpdateSQLParser("update user set name=?, age");
        assertEquals(sqlParser.getColumnValues(), Collections.singletonList("name"));
    }

    /**
     * 字符串字面量里的逗号与等号不应干扰列名提取
     */
    @Test
    void setClauseWithCommaInStringLiteral() {
        UpdateSQLParser sqlParser = new UpdateSQLParser("update user set info='a,b=c', name=?");
        assertEquals(sqlParser.getColumnValues(), Arrays.asList("info", "name"));
    }
}