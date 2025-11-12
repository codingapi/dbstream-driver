package com.codingapi.dbstream.event;

import java.util.List;

/**
 * 事件推送者
 */
public interface DBEventPusher {

    /**
     * 推送事件
     */
    void push(List<DBEvent> events);

}
