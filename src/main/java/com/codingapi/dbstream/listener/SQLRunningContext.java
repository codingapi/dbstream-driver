package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.listener.dbevent.DeleteEventListener;
import com.codingapi.dbstream.listener.dbevent.InsertEventListener;
import com.codingapi.dbstream.listener.dbevent.UpdateEventListener;
import lombok.Getter;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQLExecuteListener 上下文对象
 */
public class SQLRunningContext {

    @Getter
    private final static SQLRunningContext instance = new SQLRunningContext();

    @Getter
    private final List<SQLExecuteListener> listeners = new CopyOnWriteArrayList<>();

    private final InsertEventListener insertEventListener = new InsertEventListener();
    private final DeleteEventListener deleteEventListener = new DeleteEventListener();
    private final UpdateEventListener updateEventListener = new UpdateEventListener();

    /**
     * 添加系统默认订阅器
     */
    public void addSystemListeners() {
        this.addListener(insertEventListener);
        this.addListener(deleteEventListener);
        this.addListener(updateEventListener);
    }

    private SQLRunningContext() {

    }

    /**
     * 添加SQL执行监听器
     */
    public void addListener(SQLExecuteListener listener) {
        if (listener != null) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
        listeners.sort(Comparator.comparingInt(SQLExecuteListener::order));
    }


    /**
     * SQL执行后拦截
     */
    public void after(SQLRunningState executeState, Object result) throws SQLException {
        executeState.setResult(result);
        executeState.after();
        for (SQLExecuteListener listener : listeners) {
            listener.after(executeState, result);
        }
    }


    /**
     * SQL执行前拦截
     */
    public void before(SQLRunningState executeState) throws SQLException {
        executeState.begin();
        for (SQLExecuteListener listener : listeners) {
            listener.before(executeState);
        }
    }


    /**
     * 清空自定义的订阅
     */
    public void cleanCustomListeners() {
        listeners.removeIf(listener ->
                !(listener.equals(insertEventListener)
                        || listener.equals(updateEventListener)
                        || listener.equals(deleteEventListener))
        );
    }
}
