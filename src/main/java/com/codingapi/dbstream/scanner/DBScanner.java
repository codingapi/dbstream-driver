package com.codingapi.dbstream.scanner;

import com.codingapi.dbstream.serializable.DBTableSerializableHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

public class DBScanner {

    private final Connection connection;
    private final DBMetaData dbMetaData;
    private final DatabaseMetaData metaData;
    private final String catalog;
    private final String schema;
    private final DBTableSerializableHelper dbTableSerializableHelper;

    public DBScanner(Connection connection, Properties info) throws SQLException {
        this.connection = connection;
        this.schema = connection.getSchema();
        this.metaData = connection.getMetaData();
        this.catalog = connection.getCatalog();
        this.dbMetaData = new DBMetaData(info);
        this.dbTableSerializableHelper = new DBTableSerializableHelper(this.dbMetaData.getKeyJdbcKey());
    }


    private void loadDbTableInfo(String tableName, DbTable tableInfo) throws SQLException {
        String dbTableName = tableInfo.getName();
        if (dbTableSerializableHelper.hasSerialize(dbTableName)) {
            DbTable dbTableCache = dbTableSerializableHelper.deserialize(dbTableName);
            tableInfo.setColumns(dbTableCache.getColumns());
            tableInfo.setPrimaryKeys(dbTableCache.getPrimaryKeys());
            tableInfo.reloadPrimaryColumns();
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
        tableInfo.reloadPrimaryColumns();

        dbTableSerializableHelper.serialize(tableInfo);
    }

    /**
     * 扫描数据库中的所有表、字段和主键信息
     */
    public DBMetaData loadMetadata() throws SQLException {
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
        return dbMetaData;
    }


    /**
     * 更新对应表的元数据信息
     *
     * @param dbMetaData 数据库下的所有元数据信息
     * @throws SQLException SQLException
     */
    public void updateMetadata(DBMetaData dbMetaData) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        ResultSet tables = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"});
        List<DbTable> updateList = new ArrayList<>();
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String remarks = tables.getString("REMARKS");
            if (dbMetaData.isUpdateTableMeta(tableName)) {
                DbTable tableInfo = new DbTable(tableName, remarks);
                this.loadDbTableInfo(tableName, tableInfo);
                updateList.add(tableInfo);
            }
        }
        tables.close();
        dbMetaData.updateDbTable(updateList);
        dbMetaData.success();
    }


}
