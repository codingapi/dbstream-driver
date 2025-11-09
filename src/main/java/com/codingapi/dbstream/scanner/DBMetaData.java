package com.codingapi.dbstream.scanner;

import lombok.Getter;

import java.util.List;
import java.util.Properties;
import java.util.UUID;


public class DBMetaData {

    public static final String KEY_JDBC_URL = "jdbc.url";
    public static final String KEY_JDBC_KEY = "jdbc.key";

    /**
     * 创建唯一标识
     *
     * @return uuid
     */
    private static String generateKey() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Getter
    private long updateTime = 0;

    @Getter
    private List<DbTable> tables;

    @Getter
    private final Properties properties;

    public DBMetaData(Properties properties) {
        this.properties = properties;
        this.properties.setProperty(DBMetaData.KEY_JDBC_KEY, DBMetaData.generateKey());
    }


    void setTables(List<DbTable> tables) {
        this.tables = tables;
    }

    public void success() {
        updateTime = System.currentTimeMillis();
    }

    public DbTable getTable(String tableName) {
        if (this.tables == null) {
            return null;
        }

        for (DbTable table : tables) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                return table;
            }
        }
        return null;
    }

    public String getJdbcUrl() {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(DBMetaData.KEY_JDBC_URL);
    }

    public String getKeyJdbcKey() {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(DBMetaData.KEY_JDBC_KEY);
    }

}
