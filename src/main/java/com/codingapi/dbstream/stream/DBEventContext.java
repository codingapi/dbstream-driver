package com.codingapi.dbstream.stream;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DBEventContext {

    private final List<DBEventPusher> pushers = new ArrayList<>();

    @Getter
    private final List<String> blacklist = new ArrayList<>();

    @Getter
    private final static DBEventContext instance = new DBEventContext();

    private DBEventContext() {

    }

    public void addBlackList(String tableName) {
        this.blacklist.add(tableName);
    }

    public void removeBlackList(String tableName) {
        this.blacklist.remove(tableName);
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
