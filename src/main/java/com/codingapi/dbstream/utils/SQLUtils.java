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
     * 按分隔符切分 SQL 片段，仅在「括号深度为 0 且不在字符串字面量内」时切分。
     * <p>
     * 用于处理函数调用的参数逗号，例如 {@code replace(col, ?, ?)} 中的逗号不应被视为
     * 列分隔符。支持 {@code ''} 转义的单引号，片段内容原样保留（不做转义还原）。
     */
    public static List<String> splitTopLevel(String sql, char delimiter) {
        List<String> result = new ArrayList<>();
        if (sql == null || sql.isEmpty()) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inString = false;
        int parenDepth = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '\'') {
                current.append(c);
                if (inString) {
                    // 字符串内的 '' 为转义的单引号，仍处于字符串内
                    if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                        current.append('\'');
                        i++; // 跳过下一个引号
                    } else {
                        inString = false;
                    }
                } else {
                    inString = true;
                }
            } else if (!inString) {
                if (c == '(') {
                    parenDepth++;
                    current.append(c);
                } else if (c == ')') {
                    parenDepth--;
                    current.append(c);
                } else if (c == delimiter && parenDepth == 0) {
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
     * 提取insert语句中values的内容
     */
    public static List<String> parseInsertSQLValues(String sqlValues) {
        return splitTopLevel(sqlValues, ',');
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
