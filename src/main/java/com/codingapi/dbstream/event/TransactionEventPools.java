package com.codingapi.dbstream.event;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 事务事件消息池
 */
public class TransactionEventPools {

    @Getter
    public final static TransactionEventPools instance = new TransactionEventPools();

    private final ThreadLocal<List<DBEvent>> pools = new ThreadLocal<>();

    private final ThreadLocal<Boolean> autoMode = new ThreadLocal<>();

    private TransactionEventPools() {
    }


    /**
     * 自动提交模式设置
     */
    public void setAutoCommit(boolean autoCommit) {
        autoMode.set(autoCommit);
    }

    /**
     * 是否自动提交
     */
    public boolean isAutoCommit() {
        return autoMode.get() != null && autoMode.get();
    }


    /**
     * 添加事务事件
     */
    public void addEvents(String transactionKey, List<DBEvent> events) {
        List<DBEvent> currentEvents = pools.get();
        if (currentEvents == null) {
            currentEvents = new ArrayList<>();
            pools.set(currentEvents);
        }
        currentEvents.addAll(events);

        if (this.isAutoCommit()) {
            this.commitEvents(transactionKey);
        }

    }

    /**
     * 提交消息
     *
     * @param transactionKey 事务标示
     */
    public void commitEvents(String transactionKey) {
        List<DBEvent> currentEvents = pools.get();
        if (currentEvents != null && !currentEvents.isEmpty()) {
            currentEvents.forEach(dbEvent -> {
                dbEvent.setTransactionKey(transactionKey);
            });
            DBEventContext.getInstance().push(currentEvents);
            currentEvents.clear();
        }
        this.clear();
    }

    /**
     * 回滚消息
     *
     * @param transactionKey 事务标示
     */
    public void rollbackEvents(String transactionKey) {
        this.clear();
    }

    public void clear() {
        pools.remove();
        autoMode.remove();
    }
}
