package com.codingapi.dbstream.scanner;

import com.codingapi.dbstream.serializable.DBTableSerializableHelper;
import com.codingapi.dbstream.utils.JdbcPropertyUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

/**
 * 数据库扫描工具
 */
public class DBScanner {

    private final Connection connection;
    private final Properties info;
    private final DatabaseMetaData metaData;
    private final String catalog;
    private final String schema;
    private final DBTableSerializableHelper dbTableSerializableHelper;

    public DBScanner(Connection connection, Properties info) throws SQLException {
        this.connection = connection;
        this.schema = connection.getSchema();
        this.metaData = connection.getMetaData();
        this.catalog = connection.getCatalog();
        this.info = info;
        String jdbcKey = JdbcPropertyUtils.getOrGenerateJdbcKey(info,this.schema);
        this.dbTableSerializableHelper = new DBTableSerializableHelper(jdbcKey);
    }


    private void loadDbTableInfo(String tableName, DbTable tableInfo) throws SQLException {
        String dbTableName = tableInfo.getName();
        List<String> keys = dbTableSerializableHelper.loadTablePrimaryKeysByKeyFile(tableInfo.getName());
        if (dbTableSerializableHelper.hasSerialize(dbTableName)) {
            DbTable dbTableCache = dbTableSerializableHelper.deserialize(dbTableName);
            tableInfo.setColumns(dbTableCache.getColumns());
            tableInfo.setPrimaryKeys(dbTableCache.getPrimaryKeys());
            tableInfo.validateAndAddPrimaryKeys(keys);
            tableInfo.reloadPrimaryKeyColumns();
            return;
        }

        // 获取列信息
        ResultSet columns = metaData.getColumns(catalog, schema, tableName, "%");
        while (columns.next()) {
            DbColumn column = new DbColumn();
            column.setName(columns.getString("COLUMN_NAME"));
            column.setType(columns.getString("TYPE_NAME"));
            column.setNullable(columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
            column.setComment(columns.getString("REMARKS"));
            column.setSize(columns.getInt("COLUMN_SIZE"));
            tableInfo.addColum(column);
        }
        columns.close();

        // 获取主键信息
        ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, tableName);
        while (pkRs.next()) {
            String pkColumn = pkRs.getString("COLUMN_NAME");
            tableInfo.addPrimaryKey(pkColumn);
        }
        pkRs.close();
        tableInfo.validateAndAddPrimaryKeys(keys);
        tableInfo.reloadPrimaryKeyColumns();

        dbTableSerializableHelper.serialize(tableInfo);
    }

    /**
     * 扫描数据库中的所有表、字段和主键信息，并缓存
     */
    public DBMetaData loadMetadata() throws SQLException {
        DBMetaData dbMetaData = new DBMetaData(info);
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        LinkedHashMap<String, DbTable> tableMap = new LinkedHashMap<>();
        ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String remarks = tables.getString("REMARKS");
            DbTable tableInfo = new DbTable(tableName, remarks);
            this.loadDbTableInfo(tableName, tableInfo);
            tableMap.put(tableName, tableInfo);
        }
        tables.close();
        dbMetaData.setTables(new ArrayList<>(tableMap.values()));
        dbMetaData.success();
        DBMetaContext.getInstance().update(dbMetaData);
        return dbMetaData;
    }


    /**
     * 获取元数据表信息
     *
     * @param tableNames 查询的表名称
     * @throws SQLException SQLException
     */
    public List<DbTable> findTableMetadata(List<String> tableNames) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
        List<DbTable> tableList = new ArrayList<>();
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String remarks = tables.getString("REMARKS");
            if (tableNames.contains(tableName.toUpperCase())) {
                DbTable tableInfo = new DbTable(tableName, remarks);
                this.loadDbTableInfo(tableName, tableInfo);
                tableList.add(tableInfo);
            }
        }
        tables.close();
        return tableList;
    }


}
