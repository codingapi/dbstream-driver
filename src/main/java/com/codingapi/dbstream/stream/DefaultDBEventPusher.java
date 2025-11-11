package com.codingapi.dbstream.stream;

import java.util.List;

public class DefaultDBEventPusher implements DBEventPusher {

    @Override
    public void push(List<DBEvent> events) {
        System.out.println("<=== DBStream DBEvent Total "+events.size()+" ===> ");
        for(DBEvent event:events){
            System.out.println(event);
        }
    }
}
