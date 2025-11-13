package com.codingapi.dbstream.scanner;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  数据表元信息
 */
@Getter
public class DbTable implements Serializable {

    /**
     * 表名称
     */
    private final String name;
    /**
     * 表备注
     */
    private final String comment;
    /**
     * 表字段
     */
    private final List<DbColumn> columns = new ArrayList<>();
    /**
     * 主键keys
     */
    private final List<String> primaryKeys = new ArrayList<>();
    /**
     * 主键字段（便于取值），数据来自于columns
     */
    private final List<DbColumn> primaryColumns = new ArrayList<>();

    public DbTable(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }

    /**
     * 加载主键的字段
     */
    void reloadPrimaryKeyColumns() {
        if (this.primaryKeys.isEmpty()) {
            return;
        }
        for (DbColumn column : columns) {
            column.setPrimaryKey(this.primaryKeys.contains(column.getName()));
            if (column.isPrimaryKey()) {
                this.primaryColumns.add(column);
            }
        }
    }

    void addColum(DbColumn column) {
        this.columns.add(column);
    }


    void addPrimaryKey(String key) {
        if (!this.primaryKeys.contains(key)) {
            this.primaryKeys.add(key);
        }
    }

    public boolean hasColumns() {
        return !this.columns.isEmpty();
    }

    public boolean hasPrimaryKeys() {
        return !this.primaryKeys.isEmpty();
    }

    public boolean isPrimaryKey(String columnName) {
        for (String primaryKey : this.primaryKeys) {
            return primaryKey.equalsIgnoreCase(columnName);
        }
        return false;
    }

    public DbColumn getColumnByName(String column) {
        for (DbColumn dbColumn : columns) {
            if (dbColumn.getName().equalsIgnoreCase(column)) return dbColumn;
        }
        return null;
    }

    void setColumns(List<DbColumn> columns) {
        this.columns.clear();
        this.columns.addAll(columns);
    }

    void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys.clear();
        this.primaryKeys.addAll(primaryKeys);
    }

    /**
     * 校验并添加主键字段
     * @param primaryKeys 主键字段list
     */
    void validateAndAddPrimaryKeys(List<String> primaryKeys) {
        if (primaryKeys != null && !primaryKeys.isEmpty()) {
            for (String primaryKey : primaryKeys) {
                DbColumn column = this.getColumnByName(primaryKey);
                if (column != null) {
                    this.addPrimaryKey(column.getName());
                }
            }
        }
    }
}
