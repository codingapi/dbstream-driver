package com.codingapi.dbstream.utils;

public class ResultSetUtils {

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

}
