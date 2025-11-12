package com.codingapi.dbstream.utils;

/**
 *  执行结果判断
 */
public class ResultSetUtils {

    /**
     * 是否执行不成功，受影响数据是否小于等于0
     */
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
