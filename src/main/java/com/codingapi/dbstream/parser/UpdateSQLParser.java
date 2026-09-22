package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.utils.SQLUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateSQLParser implements SQLParser {

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
    @Override
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
     * <p>
     * SET 项之间才用逗号分隔，函数调用的参数逗号（如 {@code replace(col, ?, ?)}）不参与分割，
     * 因此按括号深度与字符串状态切分；解析不出 {@code 列名=表达式} 结构的片段直接跳过。
     */
    public List<String> getColumnValues() {
        List<String> columns = new ArrayList<>();

        // 匹配 SET 后的内容
        Pattern setPattern = Pattern.compile("(?i)\\bSET\\b\\s+(.*?)(?=\\bWHERE\\b|$)", Pattern.DOTALL);
        Matcher matcher = setPattern.matcher(sql);
        if (matcher.find()) {
            String setPart = matcher.group(1).trim();

            // 按顶层逗号切分字段赋值
            for (String part : SQLUtils.splitTopLevel(setPart, ',')) {
                // SET 项形如 "列名 = 表达式"，取第一个等号左侧为列名
                int eqIndex = part.indexOf('=');
                if (eqIndex < 0) {
                    continue;
                }
                String col = part.substring(0, eqIndex).trim();

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

        return columns;
    }


    /**
     * 提取 WHERE 部分（保留后面的所有内容）
     */
    public String getWhereSQL() {
        return SQLUtils.getWhereSQL(sql);
    }


}