package com.codingapi.dbstream.sqlparser;

import com.codingapi.dbstream.utils.SQLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertSQLParser {

    // 匹配 INSERT INTO table_name (...)
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
            "(?i)INSERT\\s+INTO\\s+([`\"\\[]?[\\w.]+[`\"\\]]?)",
            Pattern.CASE_INSENSITIVE
    );

    // 匹配列部分：(col1, col2, col3)
    private static final Pattern COLUMNS_PATTERN = Pattern.compile(
            "\\(([^)]+)\\)\\s*(?i)(VALUES|SELECT)"
    );

    private final String sql;

    public InsertSQLParser(String sql) {
        this.sql = SQLUtils.normalize(sql);
    }

    /**
     * 提取表名
     * 示例: INSERT INTO `user` (id,name) VALUES (?,?)  → user
     */
    public String getTableName() {
        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        if (matcher.find()) {
            return SQLUtils.stripQuotes(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取插入的字段名称
     * 示例: INSERT INTO user (id,name,age) VALUES (?,?,?)
     * 输出: ["id", "name", "age"]
     */
    public List<String> getColumnValues() {
        Matcher matcher = COLUMNS_PATTERN.matcher(sql);
        if (matcher.find()) {
            String cols = matcher.group(1);
            String[] parts = cols.split(",");
            List<String> columns = new ArrayList<>();
            for (String col : parts) {
                col = SQLUtils.stripQuotes(col.trim());
                if (!col.isEmpty()) {
                    columns.add(col);
                }
            }
            return columns;
        }
        return new ArrayList<>();
    }

    /**
     * 提取 SELECT 或 VALUES 后面的 SQL 内容（包含完整结构）
     * 示例:
     *   INSERT INTO user (id,name) SELECT id,name FROM other
     *   → SELECT id,name FROM other
     *
     *   INSERT INTO user (id,name) VALUES (?,?)
     *   → VALUES (?,?)
     */
    public String getSelectSQL() {
        // 匹配 VALUES 或 SELECT 后面的内容
        Pattern p = Pattern.compile("(?i)\\b(SELECT)\\b\\s+(.*)$", Pattern.DOTALL);
        Matcher m = p.matcher(sql);
        if (m.find()) {
            String keyword = m.group(1).toUpperCase();
            String rest = m.group(2).trim();
            return keyword + " " + rest;
        }
        return null;
    }

}