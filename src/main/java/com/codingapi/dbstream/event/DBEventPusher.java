package com.codingapi.dbstream.event;

import com.codingapi.dbstream.query.JdbcQuery;

import java.util.List;

/**
 * 事件推送者
 */
public interface DBEventPusher {

    /**
     * 推送事件
     * @param jdbcQuery JDBC数据查询对象
     * @param events DB事件消息
     */
    void push(JdbcQuery jdbcQuery,List<DBEvent> events);

}
