package com.codingapi.dbstream.event;

import com.codingapi.dbstream.query.JdbcQuery;

import java.util.List;

/**
 * 默认事件推送者
 * 仅执行事件打印
 */
public class DefaultDBEventPusher implements DBEventPusher {

    @Override
    public void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
        System.out.println("<=== DBStream DBEvent Total " + events.size() + " ===> ");
        for (DBEvent event : events) {
            System.out.println(event);
        }
    }
}
