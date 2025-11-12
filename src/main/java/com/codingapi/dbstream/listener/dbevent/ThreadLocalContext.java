package com.codingapi.dbstream.listener.dbevent;

import com.codingapi.dbstream.parser.DBEventParser;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQLStreamExecuteListener 下的线程上下文操作对象
 */
class ThreadLocalContext {

    /**
     * 缓存的数据，key 为index对应batch数据的顺序 value为 相应的事件解析对象
     */
    private final Map<Integer, DBEventParser> cache;

    private final ThreadLocal<ThreadLocalContext> threadLocal = new ThreadLocal<>();

    private ThreadLocalContext() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Getter
    private final static ThreadLocalContext instance = new ThreadLocalContext();

    public void remove() {
        this.threadLocal.remove();
    }

    public void push(DBEventParser eventParser) {
        this.push(0, eventParser);
    }

    public void push(int index, DBEventParser eventParser) {
        ThreadLocalContext context = threadLocal.get();
        if (context == null) {
            context = new ThreadLocalContext();
            threadLocal.set(context);
        }
        context.cache.put(index, eventParser);
    }

    public DBEventParser get() {
        return this.get(0);
    }

    public DBEventParser get(int index) {
        ThreadLocalContext context = threadLocal.get();
        if (context == null) {
            return null;
        }
        return context.cache.get(index);
    }
}
