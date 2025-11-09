package com.codingapi.dbstream.scanner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class DBScanner {

    private final Connection connection;

    public DBScanner(Connection connection) {
        this.connection = connection;
    }

    /**
     * 扫描数据库中的所有表、字段和主键信息
     */
    public DBMetaData loadMetadata(Properties info) throws SQLException {
        DBMetaData dbMetaData = new DBMetaData(info);
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
        Map<String, DbTable> tableInfoMap = new LinkedHashMap<>();
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String remarks = tables.getString("REMARKS");
            DbTable tableInfo = new DbTable(tableName, remarks);

            // 获取列信息
            ResultSet columns = metaData.getColumns(catalog, schema, tableName, "%");
            while (columns.next()) {
                DbColumn column = new DbColumn();
                column.setName(columns.getString("COLUMN_NAME"));
                column.setType(columns.getString("TYPE_NAME"));
                column.setNullable(columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.setComment(columns.getString("REMARKS"));
                column.setSize(columns.getInt("COLUMN_SIZE"));
                tableInfo.getColumns().add(column);
            }
            columns.close();

            // 获取主键信息
            ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, tableName);
            while (pkRs.next()) {
                String pkColumn = pkRs.getString("COLUMN_NAME");
                tableInfo.getPrimaryKeys().add(pkColumn);
            }
            pkRs.close();
            tableInfo.reloadPrimaryColumns();

            tableInfoMap.put(tableName, tableInfo);
        }
        tables.close();
        dbMetaData.setTables(new ArrayList<>(tableInfoMap.values()));
        dbMetaData.success();
        return dbMetaData;
    }


}
