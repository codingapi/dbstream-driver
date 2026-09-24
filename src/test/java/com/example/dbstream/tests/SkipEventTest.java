package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.event.EventType;
import com.codingapi.dbstream.query.JdbcQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * 事件跳过（skip）控制测试。
 * <p>
 * 每个用例自包含：清理状态 → 注册断言 pusher → 打跳过标记 → 执行 DML。
 * 手动事务下事件在 Spring 提交时统一推送，pusher 断言仅关注本用例 id 域内的事件
 * （清理数据产生的 DELETE 事件等被 id 域过滤排除）。
 */
@SpringBootTest
class SkipEventTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 清理状态：清空 pusher 与跳过标记，清空测试表数据
     */
    private void cleanState() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().clearSkipEvents();
        jdbcTemplate.update("delete from m_user_2");
    }

    private void insert(long id, String username) {
        jdbcTemplate.update("insert into m_user_2 (id,username,password,email,nickname) values (?,?,?,?,?)",
                id, username, "pwd", username + "@example.com", username);
    }

    private int countById(long id) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from m_user_2 where id = ?", Integer.class, id);
        return count == null ? 0 : count;
    }

    /**
     * 提取指定类型事件在本用例 id 域内的主键值列表（字符串化并排序，屏蔽 JDBC 数值类型差异）
     */
    private static List<String> ids(List<DBEvent> events, Predicate<DBEvent> typeFilter, String... scope) {
        Set<String> scopeSet = new HashSet<>(Arrays.asList(scope));
        return events.stream()
                .filter(typeFilter)
                .map(event -> String.valueOf(event.getData().get("ID")))
                .filter(scopeSet::contains)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 表级跳过 INSERT：一次性消费 + 数据照常入库 + 后续事件恢复正常
     */
    @Test
    @Transactional
    @Rollback(false)
    void test1SkipTableLevelInsert() {
        cleanState();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                // id=1001 的 INSERT 事件被跳过，仅收到 id=1002 的事件
                assertEquals(Collections.singletonList("1002"),
                        ids(events, DBEvent::isInsert, "1001", "1002"));
            }
        });

        // 表名小写传入，验证与元数据规范大小写（H2 下 M_USER_2）忽略大小写匹配
        DBStreamContext.getInstance().skipNextEvent("m_user_2", EventType.INSERT);
        insert(1001, "skipme");
        // 事件被跳过，但数据照常入库
        assertEquals(1, countById(1001));

        // 一次性消费：标记已失效，下一条 INSERT 事件恢复正常
        insert(1002, "keepme");
        assertEquals(1, countById(1002));
    }

    /**
     * 行级跳过：单语句多行 VALUES，按数据列值 Map 匹配。
     * 验证列名忽略大小写（"id" vs 元数据 "ID"）与数值类型兼容（Integer 201 vs Long 201）。
     */
    @Test
    @Transactional
    @Rollback(false)
    void test2SkipRowByDataMatch() {
        cleanState();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                // 仅 id=201 的事件被跳过
                assertEquals(Arrays.asList("202", "203"),
                        ids(events, DBEvent::isInsert, "201", "202", "203"));
            }
        });

        DBStreamContext.getInstance().skipNextEvent("M_USER_2", EventType.INSERT,
                Collections.singletonMap("id", 201));

        jdbcTemplate.update("insert into m_user_2 (id,username,password,email,nickname) values "
                + "(201,'u201','pwd','u201@example.com','u201'),"
                + "(202,'u202','pwd','u202@example.com','u202'),"
                + "(203,'u203','pwd','u203@example.com','u203')");

        // 三行数据均照常入库
        assertEquals(1, countById(201));
        assertEquals(1, countById(202));
        assertEquals(1, countById(203));
    }

    /**
     * 行级跳过：自定义 Predicate 断言。
     * 验证"实际抑制了事件才消费"——未命中的语句不消费规则，命中一次后规则失效。
     */
    @Test
    @Transactional
    @Rollback(false)
    void test3SkipRowByPredicate() {
        cleanState();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                // id=302 被跳过；301 未命中规则（规则保留）；303 时规则已消费，事件恢复
                assertEquals(Arrays.asList("301", "303"),
                        ids(events, DBEvent::isInsert, "301", "302", "303"));
            }
        });

        DBStreamContext.getInstance().skipNextEvent("m_user_2", EventType.INSERT,
                (Predicate<DBEvent>) event -> "skipme".equals(event.getData().get("USERNAME")));

        insert(301, "keepme");   // 未命中，事件正常，规则保留
        insert(302, "skipme");   // 命中，事件跳过，规则消费
        insert(303, "skipme");   // 规则已消费，事件恢复正常

        assertEquals(1, countById(302));
    }

    /**
     * 表级跳过 UPDATE 与 DELETE：各自一次性消费，数据变更照常生效
     */
    @Test
    @Transactional
    @Rollback(false)
    void test4SkipUpdateAndDelete() {
        cleanState();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(Arrays.asList("401", "402"),
                        ids(events, DBEvent::isInsert, "401", "402"));
                // id=401 的 UPDATE 事件被跳过
                assertEquals(Collections.singletonList("402"),
                        ids(events, DBEvent::isUpdate, "401", "402"));
                // id=401 的 DELETE 事件被跳过
                assertEquals(Collections.singletonList("402"),
                        ids(events, DBEvent::isDelete, "401", "402"));
            }
        });

        insert(401, "u401");
        insert(402, "u402");

        // 跳过下一条 UPDATE 事件
        DBStreamContext.getInstance().skipNextEvent("m_user_2", EventType.UPDATE);
        jdbcTemplate.update("update m_user_2 set password = 'x' where id = 401");
        // 一次性消费：下一条 UPDATE 事件恢复正常
        jdbcTemplate.update("update m_user_2 set password = 'y' where id = 402");

        // 跳过下一条 DELETE 事件
        DBStreamContext.getInstance().skipNextEvent("m_user_2", EventType.DELETE);
        jdbcTemplate.update("delete from m_user_2 where id = 401");
        // 一次性消费：下一条 DELETE 事件恢复正常
        jdbcTemplate.update("delete from m_user_2 where id = 402");

        // 更新与删除照常生效
        assertEquals(0, countById(401));
        assertEquals(0, countById(402));
    }

    /**
     * JDBC batch（addBatch/executeBatch）下的行级跳过：仅命中行被跳过，其余行事件正常
     */
    @Test
    @Transactional
    @Rollback(false)
    void test5SkipInJdbcBatch() {
        cleanState();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(Arrays.asList("501", "503"),
                        ids(events, DBEvent::isInsert, "501", "502", "503"));
            }
        });

        DBStreamContext.getInstance().skipNextEvent("m_user_2", EventType.INSERT,
                Collections.singletonMap("ID", 502L));

        jdbcTemplate.batchUpdate("insert into m_user_2 (id,username,password,email,nickname) values (?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        long id = 501 + i;
                        ps.setLong(1, id);
                        ps.setString(2, "batch" + id);
                        ps.setString(3, "pwd");
                        ps.setString(4, "batch@example.com");
                        ps.setString(5, "batch");
                    }

                    @Override
                    public int getBatchSize() {
                        return 3;
                    }
                });

        assertEquals(1, countById(501));
        assertEquals(1, countById(502));
        assertEquals(1, countById(503));
    }

    /**
     * clearSkipEvents 兜底：打标记后未执行 SQL 即清除，后续事件正常推送
     */
    @Test
    @Transactional
    @Rollback(false)
    void test6ClearSkipEvents() {
        cleanState();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(Collections.singletonList("601"),
                        ids(events, DBEvent::isInsert, "601"));
            }
        });

        // 不限类型的表级标记
        DBStreamContext.getInstance().skipNextEvent("m_user_2");
        // 未执行 SQL 即兜底清理
        DBStreamContext.getInstance().clearSkipEvents();

        insert(601, "u601");
        assertEquals(1, countById(601));
    }

}
