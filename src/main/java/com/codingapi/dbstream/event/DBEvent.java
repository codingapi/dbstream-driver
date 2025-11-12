package com.codingapi.dbstream.event;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 数据变更事件
 */
@Getter
public class DBEvent {

    /**
     * 数据表名称
     */
    private final String tableName;
    /**
     * 操作类型
     */
    private final EventType type;
    /**
     * 操作数据
     */
    private final Map<String, Object> data;
    /**
     * 数据库主键信息
     */
    private final List<String> primaryKeys;

    /**
     * 数据源的连接信息
     */
    private final String jdbcUrl;

    /**
     * 数据源的唯一标识
     * jdbcKey = sha256(jdbcUrl+schema)
     */
    private final String jdbcKey;

    /**
     * 产生数据实际
     */
    @Setter
    private long pushTimestamp;

    /**
     * 创建记录时间
     */
    private final long timestamp;

    /**
     * 事务标示
     */
    @Getter
    private String transactionKey;

    @Override
    public String toString() {
        return "DBEvent{" +
                "tableName='" + tableName + '\'' +
                ", type=" + type +
                ", data=" + data +
                ", primaryKeys=" + primaryKeys +
                ", transactionKey=" + transactionKey +
                ", jdbcKey=" + jdbcKey +
                ", timestamp=" + timestamp +
                '}';
    }

    public DBEvent(String jdbcUrl, String jdbcKey, String tableName, EventType type) {
        this.jdbcKey = jdbcKey;
        this.jdbcUrl = jdbcUrl;
        this.data = new HashMap<>();
        this.primaryKeys = new ArrayList<>();
        this.tableName = tableName;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public void set(String key, Object value) {
        this.data.put(key, value);
    }

    void setTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public void addPrimaryKey(String primaryKey) {
        if (!this.primaryKeys.contains(primaryKey)) {
            this.primaryKeys.add(primaryKey);
        }
    }


    public boolean hasPrimaryKeys() {
        return this.primaryKeys != null && !this.primaryKeys.isEmpty();
    }

}
