package com.codingapi.dbstream.interceptor;

import com.codingapi.dbstream.listener.SQLExecuteListener;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLRunningContext implements SQLExecuteListener {

    @Getter
    private final static SQLRunningContext instance = new SQLRunningContext();

    @Getter
    private final List<SQLExecuteListener> listeners = new ArrayList<>();

    private SQLRunningContext() {

    }

    public void addListener(SQLExecuteListener listener) {
        listeners.add(listener);
    }


    @Override
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        executeState.setResult(result);
        for (SQLExecuteListener listener : listeners) {
            listener.after(executeState, result);
        }
    }


    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        for (SQLExecuteListener listener : listeners) {
            listener.before(executeState);
        }
    }
}
