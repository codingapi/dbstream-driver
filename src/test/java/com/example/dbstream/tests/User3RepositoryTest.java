package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.query.JdbcQuery;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.example.dbstream.entity.User3;
import com.example.dbstream.repository.User3Repository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class User3RepositoryTest {

    @Autowired
    private User3Repository userRepository;

    /**
     * 执行内置函数插入测试
     * ！！直接将函数当作字符串传递了，实际开发过程中慎用该模式
     */
    @Test
    @Transactional
    @Rollback(value = false)
    @Order(1)
    void test1() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(1, events.size());
                for (DBEvent event : events) {
                    assertTrue(event.hasPrimaryKeys());
                    assertEquals("default", event.getData().get("ID"));
                }
            }
        });

        userRepository.insertNowValue("admin");
    }


    /**
     * 测试指定表的 metadata更新
     */
    @Test
    @Transactional
    @Rollback(value = false)
    @Order(2)
    void test2() {
        List<String> jdbcKeys = DBStreamContext.getInstance().loadDbKeys();
        assertEquals(1, jdbcKeys.size());
        for (String jdbcKey : jdbcKeys) {
            DBMetaData dbMetaData = DBStreamContext.getInstance().getMetaData(jdbcKey);
            assertNotNull(dbMetaData);
            dbMetaData.addUpdateSubscribe("m_user_3");
        }
        DBStreamContext.getInstance().cleanEventPushers();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(1, events.size());
                for (DBEvent event : events) {
                    assertTrue(event.hasPrimaryKeys());
                    assertTrue(event.isInsert());
                }
            }
        });

        User3 user = new User3();
        user.setEmail("admin@example.com");
        user.setUsername("admin");
        user.setPassword("123");
        user.setNickname("admin");
        userRepository.save(user);

    }


    /**
     * 测试清空 metadata
     */
    @Test
    @Transactional
    @Rollback(value = false)
    @Order(3)
    void test3() {
        DBStreamContext.getInstance().clearAll();
        DBStreamContext.getInstance().cleanEventPushers();

        AtomicBoolean running = new AtomicBoolean(false);

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                running.set(true);
            }
        });

        User3 user = new User3();
        user.setEmail("admin@example.com");
        user.setUsername("admin");
        user.setPassword("123");
        user.setNickname("admin");
        userRepository.save(user);

        assertFalse(running.get());
    }



}
