package com.codingapi.dbstream.interceptor;

import com.codingapi.dbstream.listener.SQLExecuteListener;
import lombok.Getter;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQLExecuteListener 上下文对象
 */
public class SQLExecuteListenerContext {

    @Getter
    private final static SQLExecuteListenerContext instance = new SQLExecuteListenerContext();

    @Getter
    private final List<SQLExecuteListener> listeners = new CopyOnWriteArrayList<>();

    private SQLExecuteListenerContext() {

    }

    /**
     * 添加SQL执行监听器
     */
    public void addListener(SQLExecuteListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }

        listeners.sort(Comparator.comparingInt(SQLExecuteListener::order));
    }


    /**
     * SQL执行后拦截
     */
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        executeState.setResult(result);
        executeState.after();
        for (SQLExecuteListener listener : listeners) {
            listener.after(executeState, result);
        }
    }


    /**
     * SQL执行前拦截
     */
    public void before(SQLExecuteState executeState) throws SQLException {
        executeState.begin();
        for (SQLExecuteListener listener : listeners) {
            listener.before(executeState);
        }
    }
}
