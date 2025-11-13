package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.query.JdbcQuery;
import com.example.dbstream.entity.User2;
import com.example.dbstream.listener.MySQLListener;
import com.example.dbstream.repository.User2Repository;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class User2RepositoryTest {

    @Autowired
    private User2Repository userRepository;

    /**
     * 常用操作测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(1)
    void test1() {
        DBStreamContext.getInstance().addListener(new MySQLListener());

        DBStreamContext.getInstance().addListener(new MySQLListener());

        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertTrue(events.size()>=4);
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                }
            }
        });

        userRepository.deleteAll();
        User2 user = new User2();
        user.setId(1);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);

        user.setPassword("123456");
        userRepository.save(user);

        int counts = userRepository.counts();
        System.out.println("counts: " + counts);

        int updateRows = userRepository.resetPassword("admin");
        System.out.println("updateRows:" + updateRows);

        User2 current = userRepository.getUserById(user.getId());
        System.out.println("current:" + current);

        int deleteRows = userRepository.deleteByUsername("admin");
        System.out.println("deleteRows:" + deleteRows);

        userRepository.deleteAll();
    }

    /**
     * 添加数据测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(2)
    void test2() {
        DBStreamContext.getInstance().cleanEventPushers();

        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(1, events.size());
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                    assertTrue(event.isInsert());
                }
            }
        });

        User2 user = new User2();
        user.setId(2);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);
    }


    /**
     * 修改功能测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(3)
    void test3() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                    assertTrue(event.isUpdate());
                }
            }
        });

        userRepository.resetPassword("123456");
    }

    /**
     * 添加并修改测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(4)
    void test4() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertTrue(events.size()>=2);
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                }
            }
        });

        User2 user = new User2();
        user.setId(4);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);
        userRepository.updatePasswordByUsername("123456", "admin");
    }

    /**
     * 删除测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(5)
    void test5() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                for (DBEvent event:events){
                    assertTrue(event.isInsert());
                    assertTrue(event.hasPrimaryKeys());
                }
            }
        });

        User2 user = new User2();
        user.setId(5);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");
        userRepository.save(user);
    }


    /**
     * 异常回滚测试
     */
    @Test
    @Transactional
    @Order(6)
    void test6() {
        DBStreamContext.getInstance().cleanEventPushers();
        AtomicBoolean running = new AtomicBoolean(false);
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                running.set(true);
            }
        });

        User2 user = new User2();
        user.setId(6);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");
        userRepository.save(user);

        assertThrows(ArithmeticException.class, () -> {
            int result = 100 / 0;
            System.out.println(result);
        });

        assertFalse(running.get());
    }


    /**
     * 修改功能测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(7)
    void test7() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertTrue(events.size()>=2);
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                }
            }
        });

        User2 user = new User2();
        user.setId(7);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");
        userRepository.save(user);

        userRepository.resetPasswordByUsername1("admin");
    }


    /**
     * 修改功能测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(8)
    void test8() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(1,events.size());
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                }
            }
        });

        userRepository.staticSave();
    }

}
