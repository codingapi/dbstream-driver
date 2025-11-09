package com.codingapi.dbstream.stream;

import java.util.List;

public interface DBEventPusher {

    void push(List<DBEvent> events);

}
