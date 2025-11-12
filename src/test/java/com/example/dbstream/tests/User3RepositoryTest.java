package com.example.dbstream.tests;


import com.example.dbstream.repository.User3Repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;


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
    void test1() {
        userRepository.insertNowValue("admin");
    }


}
