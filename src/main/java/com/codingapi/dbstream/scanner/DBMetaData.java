package com.codingapi.dbstream.scanner;

import com.codingapi.dbstream.utils.DBTableSerializableHelper;
import com.codingapi.dbstream.utils.SHA256Util;
import lombok.Getter;

import java.util.List;
import java.util.Properties;


public class DBMetaData {

    public static final String KEY_JDBC_URL = "jdbc.url";
    public static final String KEY_JDBC_KEY = "jdbc.key";

    /**
     * 创建唯一标识
     *
     * @return sha256(jdbcUrl+schema)
     */
    private String generateKey(String schema) {
        String jdbcUrl = this.getJdbcUrl();
        String data = String.format("%s#%s", jdbcUrl, schema == null ? "" : schema);
        return SHA256Util.sha256(data);
    }

    @Getter
    private long updateTime = 0;

    @Getter
    private List<DbTable> tables;

    @Getter
    private final Properties properties;

    public DBMetaData(Properties properties, String schema) {
        this.properties = properties;
        this.properties.setProperty(DBMetaData.KEY_JDBC_KEY, this.generateKey(schema));
    }


    void setTables(List<DbTable> tables) {
        this.tables = tables;
    }

    public void success() {
        updateTime = System.currentTimeMillis();
    }

    public boolean isEmpty() {
        return this.tables == null || this.tables.isEmpty();
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

    public void clean(){
        DBTableSerializableHelper tableSerializableHelper = new DBTableSerializableHelper(this.getKeyJdbcKey());
        tableSerializableHelper.clean();
        this.tables = null;
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
