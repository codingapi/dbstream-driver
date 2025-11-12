package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.utils.SQLUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteSQLParser implements SQLParser {

    private final String sql;


    // 匹配 INSERT INTO table_name (...)
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
            "(?i)DELETE(?:\\s+[\\w,\\s]+)?\\s+FROM\\s+([\\w\\.\\[\\]\"`]+)",
            Pattern.CASE_INSENSITIVE
    );

    public DeleteSQLParser(String sql) {
        this.sql = SQLUtils.normalize(sql);
    }

    /**
     * 提取表名（兼容 schema.table, 支持方括号/引号）
     */
    public String getTableName() {
        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        if (matcher.find()) {
            return SQLUtils.stripQuotes(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取别名（支持 AS、方括号/引号；避免把 SQL 关键字当成别名）
     */
    public String getTableAlias() {
        if (sql.isEmpty()) return null;

        // 1. 获取表名
        String tableName = getTableName();
        if (tableName == null || tableName.isEmpty()) return null;

        // 2. 构造匹配模式：FROM tableName [AS] alias
        String regex = String.format(
                "(?i)FROM\\s+[`\"\\[]?%s[`\"\\]]?(?:\\s+AS)?\\s+([A-Za-z_][A-Za-z0-9_]*)",
                Pattern.quote(tableName)
        );
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String alias = matcher.group(1);
            // 避免误匹配 SQL 关键字
            if (!SQLUtils.isSQLKeyword(alias)) {
                return alias;
            }
        }
        return null;
    }


    /**
     * 提取 WHERE 部分（保留后面的所有内容）
     */
    public String getWhereSQL() {
        return SQLUtils.getWhereSQL(sql);
    }


}