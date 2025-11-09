package com.codingapi.dbstream.utils;

import java.util.regex.Pattern;

public class SQLParamUtils {

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

    public static int paramsCount(String sql) {
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


    public static boolean isNotUpdatedRows(Object result) {
        if (result instanceof Integer) {
            int value = (Integer) result;
            return value <= 0;
        }

        if (result instanceof Long) {
            long value = (Long) result;
            return value <= 0;
        }
        return false;
    }

    public static boolean isUpdateSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return UPDATE_SQL_PATTERN.matcher(sql.trim()).matches();
    }

    public static boolean isInsertSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return INSERT_SQL_PATTERN.matcher(sql.trim()).matches();
    }

    public static boolean isDeleteSQL(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        return DELETE_SQL_PATTERN.matcher(sql.trim()).matches();
    }


}
