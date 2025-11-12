package com.codingapi.dbstream.event;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件控制上下文对象
 */
public class DBEventContext {

    private final List<DBEventPusher> pushers = new CopyOnWriteArrayList<>();

    @Getter
    private final static DBEventContext instance = new DBEventContext();

    private final DefaultDBEventPusher defaultDBEventPusher = new DefaultDBEventPusher();

    private DBEventContext() {

    }

    void push(List<DBEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        events.forEach(event -> {
            event.setPushTimestamp(System.currentTimeMillis());
        });
        if (!this.pushers.isEmpty()) {
            for (DBEventPusher pusher : pushers) {
                pusher.push(events);
            }
        } else {
            defaultDBEventPusher.push(events);
        }
    }

    public void addPusher(DBEventPusher pusher) {
        if (pusher != null) {
            pushers.add(pusher);
        }
    }

}
