package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.query.JdbcQuery;
import com.example.dbstream.entity.User1;
import com.example.dbstream.listener.MySQLListener;
import com.example.dbstream.repository.User1Repository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class User1RepositoryTest {

    @Autowired
    private User1Repository userRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * 常用操作测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(1)
    void test1() {

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
        User1 user = new User1();
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

        User1 current = userRepository.getUserById(user.getId());
        System.out.println("current:" + current);

        int deleteRows = userRepository.deleteByUsername("admin");
        System.out.println("deleteRows:" + deleteRows);

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
        User1 user = new User1();
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
        User1 user = new User1();
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
        User1 user = new User1();
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);
    }


    /**
     * insert into select 测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(6)
    void test6() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertTrue(events.size()>=2);
                int hasPrimaryEventCount = 0;
                for (DBEvent event:events){
                    assertTrue(event.isInsert());
                    if(event.hasPrimaryKeys()){
                        hasPrimaryEventCount++;
                    }
                }
                assertEquals(1,hasPrimaryEventCount);
            }
        });
        User1 user = new User1();
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);

        userRepository.insertIntoFromSelect();
    }


    /**
     * 异常回滚测试
     */
    @Test
    @Transactional
    @Order(7)
    void test7() {
        DBStreamContext.getInstance().cleanEventPushers();
        AtomicBoolean running = new AtomicBoolean(false);
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
               running.set(true);
            }
        });

        User1 user = new User1();
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
     * 批量数据插入测试
     */
    @Test
    @Transactional
    @Rollback(value = false)
    @Order(8)
    void test8() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertTrue(events.size()>=15);
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                }
            }
        });

        List<User1> list = userRepository.findAll();
        for (User1 user : list) {
            user.setEmail("111");
        }
        userRepository.saveAll(list);

        userRepository.deleteAll();
        for (int i = 0; i < 5; i++) {
            User1 user1 = new User1();
            user1.setUsername("admin1");
            user1.setPassword("admin1");
            user1.setEmail("admin1@example.com");
            user1.setNickname("admin1");
            entityManager.persist(user1);
        }
        entityManager.flush();
        entityManager.clear();
    }

}
