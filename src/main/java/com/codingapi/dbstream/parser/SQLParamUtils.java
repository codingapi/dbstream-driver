package com.codingapi.dbstream.parser;

public class SQLParamUtils {


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


    public static boolean isUpdateRow(Object result) {
        if (result instanceof Integer) {
            int value = (Integer) result;
            return value > 0;
        }

        if (result instanceof Long) {
            long value = (Long) result;
            return value > 0;
        }
        return false;
    }
}
