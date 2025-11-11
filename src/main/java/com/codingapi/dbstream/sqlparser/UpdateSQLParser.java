package com.codingapi.dbstream.sqlparser;

import com.codingapi.dbstream.utils.SQLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateSQLParser {

    private final String sql;

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
            "(?i)UPDATE\\s+([\\w\\.\\[\\]\"`]+)"
    );

    public UpdateSQLParser(String sql) {
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
        if (sql == null || sql.isEmpty()) {
            return null;
        }

        String tableName = getTableName();
        if (tableName == null || tableName.isEmpty()) {
            return null;
        }

        // 匹配 UPDATE tableName [AS] alias
        String regex = "(?i)UPDATE\\s+[`\"\\[]?" + Pattern.quote(tableName)
                + "[`\"\\]]?(?:\\s+AS)?\\s+([A-Za-z_][A-Za-z0-9_]*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String alias = matcher.group(1);
            if (!SQLUtils.isSQLKeyword(alias)) {
                return alias;
            }
        }
        return null;
    }


    /**
     * 提取 SET 后面的字段名
     * 示例: SET name=?, age=?, updated_at=NOW()
     */
    public List<String> getColumnValues() {
        List<String> columns = new ArrayList<>();

        // 匹配 SET 后的内容
        Pattern setPattern = Pattern.compile("(?i)\\bSET\\b\\s+(.*?)(?=\\bWHERE\\b|$)", Pattern.DOTALL);
        Matcher matcher = setPattern.matcher(sql);
        if (matcher.find()) {
            String setPart = matcher.group(1).trim();

            // 分割字段赋值（按逗号分割）
            String[] parts = setPart.split(",");
            for (String part : parts) {
                String[] kv = part.split("=", 2);
                if (kv.length > 0) {
                    String col = kv[0].trim();

                    // 如果字段是带表别名的，如 "u.name"
                    int dotIndex = col.lastIndexOf('.');
                    if (dotIndex > 0) {
                        col = col.substring(dotIndex + 1);
                    }

                    col = SQLUtils.stripQuotes(col);
                    if (!col.isEmpty()) {
                        columns.add(col);
                    }
                }
            }
        }

        return columns;
    }


    /**
     * 提取 WHERE 部分（保留后面的所有内容）
     */
    public String getWhereSQL() {
        return SQLUtils.getWhereSQL(sql);
    }


}