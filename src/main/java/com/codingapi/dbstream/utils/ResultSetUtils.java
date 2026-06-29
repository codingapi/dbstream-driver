package com.codingapi.dbstream.utils;

import java.sql.Statement;

/**
 * 执行结果判断
 */
public class ResultSetUtils {

    /**
     * 是否执行不成功，受影响数据是否小于等于0。
     * <p>
     * 批量执行(executeBatch/executeLargeBatch)中，JDBC 驱动常用
     * {@link Statement#SUCCESS_NO_INFO}(-2) 表示「执行成功但具体行数未知」，
     * 此时应视为已更新，否则会导致整批事件被静默丢弃。
     */
    public static boolean isNotUpdatedRows(Object result) {
        if (result instanceof Integer) {
            int value = (Integer) result;
            if (value == Statement.SUCCESS_NO_INFO) {
                return false;
            }
            return value <= 0;
        }

        if (result instanceof Long) {
            long value = (Long) result;
            if (value == Statement.SUCCESS_NO_INFO) {
                return false;
            }
            return value <= 0;
        }
        return false;
    }

}
