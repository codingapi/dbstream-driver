package com.codingapi.dbstream.scanner;

import com.codingapi.dbstream.serializable.DBTableSerializableHelper;
import lombok.Getter;

import java.util.*;

/**
 * 数据库所有表的元数据信息
 */
public class DBMetaData {

    public static final String KEY_JDBC_URL = "jdbc.url";
    public static final String KEY_JDBC_KEY = "jdbc.key";

    /**
     * 待更新的table meta数据
     */
    @Getter
    private final List<String> subjectTableNameList = new ArrayList<>();

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

    public DBMetaData(Properties properties) {
        this.properties = properties;
    }

    /**
     * 添加元数据信息更新订阅
     * 当增加数据之后，将会再下次执行改变的数据事件分析前，进行元数据的重新加载。
     * 对新增加的表，也可通过该还是
     *
     * @param tableName 表名称
     */
    public void addUpdateSubscribe(String tableName) {
        String upTableName = tableName.toUpperCase();
        if (!this.subjectTableNameList.contains(upTableName)) {
            this.subjectTableNameList.add(upTableName);
        }
    }

    /**
     * 更新全部元数据信息
     */
    void setTables(List<DbTable> tables) {
        this.tables = tables;
    }

    /**
     * 记录数据更新时间
     */
    void success() {
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
        tableSerializableHelper.remove();
        this.tables.clear();
        this.subjectTableNameList.clear();
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
     * 是否为订阅数据更新
     *
     * @param tableName 表名称
     * @return true 是
     */
    public boolean isSubjectUpdate(String tableName) {
        return this.subjectTableNameList.contains(tableName.toUpperCase());
    }

    /**
     * 更新DbTable数据
     *
     * @param updateList 更新的表元数据信息
     */
    void updateDbTable(List<DbTable> updateList) {
        if (this.tables == null || this.tables.isEmpty()) {
            this.tables = new ArrayList<>(updateList);
            return;
        }

        List<DbTable> list = new ArrayList<>();
        Map<String, DbTable> updateDbTables = new HashMap<>();
        for (DbTable update : updateList) {
            String tableName = update.getName().toUpperCase();
            updateDbTables.put(tableName, update);
            this.subjectTableNameList.remove(tableName);
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

        // 添加新增的表（在 updateList 中但不在原有 tables 中的表）
        for (DbTable update : updateList) {
            String tableName = update.getName().toUpperCase();
            boolean exists = false;
            for (DbTable existing : this.tables) {
                if (existing.getName().equalsIgnoreCase(tableName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                list.add(update);
            }
        }

        this.tables = list;
    }
}
