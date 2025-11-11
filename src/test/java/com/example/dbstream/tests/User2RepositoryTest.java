package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.example.dbstream.entity.User2;
import com.example.dbstream.listener.MySQLListener;
import com.example.dbstream.repository.User2Repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;


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
    void test1() {
        DBStreamContext.getInstance().addListener(new MySQLListener());

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
    void test2() {
        userRepository.deleteAll();
        User2 user = new User2();
        user.setId(2);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);
        userRepository.deleteAll();
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
    void test5() {
        User2 user = new User2();
        user.setId(5);
        user.setUsername("admin");
        user.setPassword("admin");
        user.setEmail("admin@example.com");
        user.setNickname("admin");

        userRepository.save(user);

        userRepository.deleteAll();
    }



    /**
     * 异常回滚测试
     */
    @Test
    @Transactional
    void test6() {
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
    }


    /**
     * 修改功能测试
     */
    @Test
    @Transactional
    @Rollback(false)
    void test7() {
        userRepository.resetPasswordByUsername1("admin");
    }



    /**
     * 修改功能测试
     */
    @Test
    @Transactional
    @Rollback(false)
    void test8() {
        userRepository.deleteAll();
        userRepository.staticSave();
    }

}
