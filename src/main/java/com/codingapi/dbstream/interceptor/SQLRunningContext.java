package com.codingapi.dbstream.interceptor;

import com.codingapi.dbstream.listener.SQLExecuteListener;
import lombok.Getter;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SQLRunningContext   {

    @Getter
    private final static SQLRunningContext instance = new SQLRunningContext();

    @Getter
    private final List<SQLExecuteListener> listeners = new CopyOnWriteArrayList<>();

    private SQLRunningContext() {

    }

    public void addListener(SQLExecuteListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }

        listeners.sort(Comparator.comparingInt(SQLExecuteListener::order));
    }


    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        executeState.setResult(result);
        executeState.after();
        for (SQLExecuteListener listener : listeners) {
            listener.after(executeState, result);
        }
    }


    public void before(SQLExecuteState executeState) throws SQLException {
        executeState.begin();
        for (SQLExecuteListener listener : listeners) {
            listener.before(executeState);
        }
    }
}
