package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.query.JdbcQuery;
import com.example.dbstream.repository.User3Repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;
import java.util.List;

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
    void test1() {
        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                assertEquals(1,events.size());
                for (DBEvent event:events){
                    assertTrue(event.hasPrimaryKeys());
                    assertEquals("default",event.getData().get("ID"));
                }
            }
        });

        userRepository.insertNowValue("admin");
    }


}
