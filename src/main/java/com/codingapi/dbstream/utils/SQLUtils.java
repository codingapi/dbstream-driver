package com.codingapi.dbstream.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL处理工具类
 */
public class SQLUtils {

    private static final Pattern DELETE_SQL_PATTERN = Pattern.compile(
            "^\\s*(?i)(DELETE)\\b.*",
            Pattern.DOTALL
    );


    private static final Pattern INSERT_SQL_PATTERN = Pattern.compile(
            "^\\s*(?i)(INSERT)\\b.*",
            Pattern.DOTALL
    );

    private static final Pattern UPDATE_SQL_PATTERN = Pattern.compile(
            "^\\s*(?i)(UPDATE)\\b.*",
            Pattern.DOTALL
    );

    /**
     * JDBC参数数量
     */
    public static int jdbcParamsCount(String sql) {
        if (sql == null || sql.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

    /**
     * 去掉外部方括号或引号
     */
    public static String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() >= 2) {
            if ((s.startsWith("[") && s.endsWith("]"))
                    || (s.startsWith("\"") && s.endsWith("\""))
                    || (s.startsWith("(") && s.endsWith(")"))
                    || (s.startsWith("`") && s.endsWith("`"))) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * sql 格式化
     */
    public static String normalize(String sql) {
        if (sql == null) return "";
        // collapse multi-space, keep original case for returned where clause but use case-insensitive regex
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 提取 WHERE 部分（保留后面的所有内容）
     */
    public static String getWhereSQL(String sql) {
        Pattern pattern = Pattern.compile("(?i)\\bWHERE\\b\\s+(.*)$");
        Matcher m = pattern.matcher(sql);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     *  提取insert语句中values的内容
     */
    public static List<String> parseInsertSQLValues(String sqlValues) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inString = false;
        int parenDepth = 0;

        for (int i = 0; i < sqlValues.length(); i++) {
            char c = sqlValues.charAt(i);

            if (c == '\'') {
                // 切换字符串状态（需处理转义的单引号）
                if (inString && i + 1 < sqlValues.length() && sqlValues.charAt(i + 1) == '\'') {
                    // SQL转义 '' -> '
                    current.append('\'');
                    i++; // 跳过下一个引号
                } else {
                    inString = !inString;
                    current.append(c);
                }
            } else if (!inString) {
                if (c == '(') {
                    parenDepth++;
                    current.append(c);
                } else if (c == ')') {
                    parenDepth--;
                    current.append(c);
                } else if (c == ',' && parenDepth == 0) {
                    // 只有在括号层为0时才分割
                    result.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    /**
     * 判断是否为常见 SQL 关键字
     */
    public static boolean isSQLKeyword(String word) {
        if (word == null) {
            return true;
        }
        String w = word.trim().toUpperCase();
        String[] keywords = {
                "WHERE", "FROM", "JOIN", "ON", "DELETE", "UPDATE", "SET",
                "VALUES", "SELECT", "AS", "INTO", "AND", "OR", "NOT"
        };
        for (String k : keywords) {
            if (k.equals(w)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 是否update SQL
     */
    public static boolean isUpdateSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return UPDATE_SQL_PATTERN.matcher(sql.trim()).matches();
    }

    /**
     * 是否insert SQL
     */
    public static boolean isInsertSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return INSERT_SQL_PATTERN.matcher(sql.trim()).matches();
    }

    /**
     * 是否delete SQL
     */
    public static boolean isDeleteSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return DELETE_SQL_PATTERN.matcher(sql.trim()).matches();
    }

}
