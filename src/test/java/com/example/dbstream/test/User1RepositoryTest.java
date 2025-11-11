package com.example.dbstream.test;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.DBEventPusher;
import com.example.dbstream.entity.User1;
import com.example.dbstream.listener.MySQLListener;
import com.example.dbstream.repository.User1Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class User1RepositoryTest {

    @Autowired
    private User1Repository userRepository;

    @BeforeEach
    void setUp() {
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(List<DBEvent> events) {
                System.out.println(events);
            }
        });
    }

    /**
     * 常用操作测试
     */
    @Test
    @Transactional
    @Rollback(false)
    void test1() {
        DBStreamContext.getInstance().addListener(new MySQLListener());

        userRepository.deleteAll();
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

        userRepository.deleteAll();
    }

    /**
     * 添加数据测试
     */
    @Test
    @Transactional
    @Rollback(false)
    void test2() {
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
    void test3() {
        userRepository.resetPassword("123456");
    }

    /**
     * 添加并修改测试
     */
    @Test
    @Transactional
    @Rollback(false)
    void test4() {
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
    void test5() {
        User1 user = new User1();
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);

        userRepository.deleteAll();
    }


    /**
     * insert into select 测试
     */
    @Test
    @Transactional
    @Rollback(false)
    void test6() {
        userRepository.insertIntoFromSelect();
    }


    /**
     * 异常回滚测试
     */
    @Test
    @Transactional
    void test7() {
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
    }

}
