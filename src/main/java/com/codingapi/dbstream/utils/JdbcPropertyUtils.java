package com.codingapi.dbstream.utils;

import com.codingapi.dbstream.scanner.DBMetaData;

import java.util.Properties;

/**
 * Jdbc属性工具类
 */
public class JdbcPropertyUtils {

    /**
     * 获取或创建数据库唯一标识信息
     */
    public static String getOrGenerateJdbcKey(Properties properties, String schema) {
        String jdbcKey = properties.getProperty(DBMetaData.KEY_JDBC_KEY);
        if (jdbcKey != null) {
            return jdbcKey;
        }
        String jdbcUrl = properties.getProperty(DBMetaData.KEY_JDBC_URL);
        String data = String.format("%s#%s", jdbcUrl, schema == null ? "" : schema);
        jdbcKey = SHA256Utils.sha256(data);
        properties.setProperty(DBMetaData.KEY_JDBC_KEY, jdbcKey);
        return jdbcKey;
    }
}
