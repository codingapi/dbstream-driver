package com.example.dbstream.tests;

import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DbTable;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MetadataRefreshTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    private static final String SUFFIX = "_" + System.nanoTime() % 100000;

    /**
     * 测试 refreshAll 全量刷新后新表能被元数据感知
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(1)
    void testRefreshAll() throws Exception {
        String tableName = "T_RA" + SUFFIX;
        entityManager.createNativeQuery(
            "CREATE TABLE " + tableName + " (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))"
        ).executeUpdate();
        entityManager.flush();

        List<String> keys = DBStreamContext.getInstance().loadDbKeys();
        assertFalse(keys.isEmpty(), "应至少有一个数据源");
        String jdbcKey = keys.get(0);

        DBMetaData metaData = DBStreamContext.getInstance().getMetaData(jdbcKey);

        try (Connection conn = dataSource.getConnection()) {
            DBStreamContext.getInstance().refreshAll(conn, jdbcKey);
        }

        DbTable afterRefresh = metaData.getTable(tableName);
        assertNotNull(afterRefresh, "刷新后新表应在元数据中");
        assertTrue(afterRefresh.hasPrimaryKeys(), "新表应有主键");
        assertTrue(afterRefresh.hasColumns(), "新表应有列");
    }

    /**
     * 测试 refreshTable 按表名刷新
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(2)
    void testRefreshTable() throws Exception {
        String tableName = "T_RT" + SUFFIX;
        entityManager.createNativeQuery(
            "CREATE TABLE " + tableName + " (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(100), val VARCHAR(255))"
        ).executeUpdate();
        entityManager.flush();

        List<String> keys = DBStreamContext.getInstance().loadDbKeys();
        String jdbcKey = keys.get(0);

        DBMetaData metaData = DBStreamContext.getInstance().getMetaData(jdbcKey);

        try (Connection conn = dataSource.getConnection()) {
            DBStreamContext.getInstance().refreshTable(conn, jdbcKey, tableName);
        }

        DbTable table = metaData.getTable(tableName);
        assertNotNull(table, "按表名刷新后应存在");
        assertTrue(table.hasPrimaryKeys());
    }

    /**
     * 测试 refreshTable 对已存在的表能更新元数据（新增列）
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(3)
    void testRefreshTableAlter() throws Exception {
        String tableName = "T_ALTER" + SUFFIX;
        entityManager.createNativeQuery(
            "CREATE TABLE " + tableName + " (id BIGINT AUTO_INCREMENT PRIMARY KEY, col1 VARCHAR(100))"
        ).executeUpdate();
        entityManager.flush();

        List<String> keys = DBStreamContext.getInstance().loadDbKeys();
        String jdbcKey = keys.get(0);
        DBMetaData metaData = DBStreamContext.getInstance().getMetaData(jdbcKey);

        // 首次刷新，获取初始元数据
        try (Connection conn = dataSource.getConnection()) {
            DBStreamContext.getInstance().refreshTable(conn, jdbcKey, tableName);
        }
        DbTable before = metaData.getTable(tableName);
        assertNotNull(before);
        int columnCountBefore = before.getColumns().size();

        // 新增列
        entityManager.createNativeQuery(
            "ALTER TABLE " + tableName + " ADD COLUMN col2 VARCHAR(200)"
        ).executeUpdate();
        entityManager.flush();

        // 再次刷新
        try (Connection conn = dataSource.getConnection()) {
            DBStreamContext.getInstance().refreshTable(conn, jdbcKey, tableName);
        }

        DbTable after = metaData.getTable(tableName);
        assertNotNull(after);
        assertTrue(after.getColumns().size() > columnCountBefore, "刷新后列数应增加");
    }
}
