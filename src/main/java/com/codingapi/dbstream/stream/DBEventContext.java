package com.codingapi.dbstream.stream;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DBEventContext {


    private final List<DBEventPusher> pushers = new ArrayList<>();

    @Getter
    private final static DBEventContext instance = new DBEventContext();

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
        }
    }

    public void addPusher(DBEventPusher pusher) {
        pushers.add(pusher);
    }

}
