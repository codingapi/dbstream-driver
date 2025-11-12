package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.event.DBEvent;

import java.sql.SQLException;
import java.util.List;

/**
 * DBEvent 事件解析
 */
public interface DBEventParser {

    /**
     * 预先处理
     */
    void prepare() throws SQLException;

    /**
     * 加载DBEvent事件数据
     *
     * @param result sql执行结果返回值
     * @return List<DBEvent>
     */
    List<DBEvent> loadEvents(Object result) throws SQLException;
}
