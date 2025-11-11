package com.codingapi.dbstream.scanner;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DbTable implements Serializable {

    private final String name;
    private final String comment;
    private final List<DbColumn> columns = new ArrayList<>();
    private final List<String> primaryKeys = new ArrayList<>();
    private final List<DbColumn> primaryColumns = new ArrayList<>();

    public DbTable(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }

    /**
     * 加载主键的字段
     */
    public void reloadPrimaryColumns() {
        if(this.primaryKeys.isEmpty()){
            return;
        }
        for (DbColumn column : columns) {
            column.setPrimaryKey(this.primaryKeys.contains(column.getName()));
            if (column.isPrimaryKey()) {
                this.primaryColumns.add(column);
            }
        }
    }

    public void addColum(DbColumn column) {
        this.columns.add(column);
    }


    public void addPrimaryKey(String key) {
        if(!this.primaryKeys.contains(key)) {
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

    public void setColumns(List<DbColumn> columns) {
        this.columns.clear();
        this.columns.addAll(columns);
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys.clear();
        this.primaryKeys.addAll(primaryKeys);
    }

    public void loadLocalPrimaryKeys(List<String> primaryKeys) {
        if(primaryKeys!=null && !primaryKeys.isEmpty()){
            for(String primaryKey:primaryKeys){
                DbColumn column = this.getColumnByName(primaryKey);
                if(column!=null){
                    this.addPrimaryKey(column.getName());
                }
            }
        }
    }
}
