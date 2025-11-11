package com.codingapi.dbstream.scanner;

import com.codingapi.dbstream.utils.DBTableSerializableHelper;
import com.codingapi.dbstream.utils.SHA256Utils;
import lombok.Getter;

import java.util.*;


public class DBMetaData {

    public static final String KEY_JDBC_URL = "jdbc.url";
    public static final String KEY_JDBC_KEY = "jdbc.key";

    /**
     * 待更新的table meta数据
     */
    @Getter
    private final List<String> updateTableMetaList = new ArrayList<>();

    /**
     * 创建唯一标识
     *
     * @return sha256(jdbcUrl+schema)
     */
    private String generateKey(String schema) {
        String jdbcUrl = this.getJdbcUrl();
        String data = String.format("%s#%s", jdbcUrl, schema == null ? "" : schema);
        return SHA256Utils.sha256(data);
    }

    /**
     * 数据记录时间
     */
    @Getter
    private long updateTime = 0;

    /**
     * 扫描的表元数据信息
     */
    @Getter
    private List<DbTable> tables;

    /**
     * jdbc 的配置信息
     */
    @Getter
    private final Properties properties;

    public DBMetaData(Properties properties, String schema) {
        this.properties = properties;
        this.properties.setProperty(DBMetaData.KEY_JDBC_KEY, this.generateKey(schema));
    }

    /**
     * 更新表的元数据信息
     *
     * @param tableName 表名称
     */
    public void addUpdateTableMateList(String tableName) {
        String upTableName = tableName.toUpperCase();
        if (this.updateTableMetaList.contains(upTableName)) {
            this.updateTableMetaList.add(upTableName);
        }
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

    /**
     * 获取表的元信息
     *
     * @param tableName 表名称
     * @return DbTable
     */
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

    /**
     * 清空序列化的本地缓存数据
     */
    void cleanSerializable() {
        DBTableSerializableHelper tableSerializableHelper = new DBTableSerializableHelper(this.getKeyJdbcKey());
        tableSerializableHelper.clean();
        this.tables = null;
    }

    /**
     * 获取jdbc的url
     *
     * @return jdbcUrl
     */
    public String getJdbcUrl() {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(DBMetaData.KEY_JDBC_URL);
    }

    /**
     * 获取jdbc的唯一值
     *
     * @return jdbcKey
     */
    public String getKeyJdbcKey() {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(DBMetaData.KEY_JDBC_KEY);
    }

    /**
     * 是否为待更新的数据库表
     *
     * @param tableName 表名称
     * @return true 是
     */
    public boolean isUpdateTableMeta(String tableName) {
        return this.updateTableMetaList.contains(tableName.toUpperCase());
    }

    /**
     * 更新DbTable数据
     *
     * @param updateList 更新的表元数据信息
     */
    void updateDbTable(List<DbTable> updateList) {
        List<DbTable> list = new ArrayList<>();
        Map<String, DbTable> updateDbTables = new HashMap<>();
        for (DbTable update : updateList) {
            String tableName = update.getName().toUpperCase();
            updateDbTables.put(tableName, update);
            this.updateTableMetaList.remove(tableName);
        }

        for (DbTable dbTable : tables) {
            String tableName = dbTable.getName().toUpperCase();
            DbTable updateTable = updateDbTables.get(tableName);
            if (updateTable != null) {
                list.add(updateTable);
            } else {
                list.add(dbTable);
            }
        }

        this.tables.clear();
        this.tables = list;
    }
}
