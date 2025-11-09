package com.codingapi.dbstream.scanner;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DbTable {

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
        for (DbColumn column : columns) {
            column.setPrimaryKey(this.primaryKeys.contains(column.getName()));
            if (column.isPrimaryKey()) {
                this.primaryColumns.add(column);
            }
        }
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
}
