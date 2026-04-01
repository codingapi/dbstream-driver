package com.example.dbstream.tests;


import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.DBEventPusher;
import com.codingapi.dbstream.query.JdbcQuery;
import com.example.dbstream.entity.User3;
import com.example.dbstream.listener.MySQLListener;
import com.example.dbstream.repository.User3Repository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class InsertBatchValuesTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private User3Repository user3Repository;

    /**
     * 常用操作测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(1)
    void test1() {

        user3Repository.deleteAll();

        DBStreamContext.getInstance().addListener(new MySQLListener());

        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                System.out.println(events);
            }
        });

        String insertSQL = "insert into m_user_3 (username,password,email,nickname) values ('1','1','1','1')";

        jdbcTemplate.update(insertSQL);

        List<User3> userList = user3Repository.findAll();

        assertEquals(1,userList.size());

    }


    /**
     * 常用操作测试
     */
    @Test
    @Transactional
    @Rollback(false)
    @Order(2)
    void test2() {

        user3Repository.deleteAll();

        DBStreamContext.getInstance().addListener(new MySQLListener());

        DBStreamContext.getInstance().cleanEventPushers();
        DBStreamContext.getInstance().addEventPusher(new DBEventPusher() {
            @Override
            public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
                System.out.println(events);
            }
        });

        String insertSQL = "insert into m_user_3 (username,password,email,nickname) values ('1','1','1','1'),('1','1','1','1')";

        jdbcTemplate.update(insertSQL);

        List<User3> userList = user3Repository.findAll();

        assertEquals(2,userList.size());

    }



}
