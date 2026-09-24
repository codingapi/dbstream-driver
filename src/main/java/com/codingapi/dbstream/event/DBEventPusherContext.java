package com.codingapi.dbstream.event;

import com.codingapi.dbstream.query.JdbcQuery;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件推送控制上下文对象
 */
public class DBEventPusherContext {

    private final List<DBEventPusher> pushers = new CopyOnWriteArrayList<>();

    @Getter
    private final static DBEventPusherContext instance = new DBEventPusherContext();

    private final DefaultDBEventPusher defaultDBEventPusher = new DefaultDBEventPusher();

    private DBEventPusherContext() {}

    void push(JdbcQuery jdbcQuery, List<DBEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        events.forEach(event -> {
            event.setPushTimestamp(System.currentTimeMillis());
        });
        if (!this.pushers.isEmpty()) {
            for (DBEventPusher pusher : pushers) {
                pusher.push(jdbcQuery, events);
            }
        } else {
            defaultDBEventPusher.push(jdbcQuery, events);
        }
    }

    public void addPusher(DBEventPusher pusher) {
        if (pusher != null) {
            pushers.add(pusher);
        }
    }

    public void clean() {
        this.pushers.clear();
    }
}
