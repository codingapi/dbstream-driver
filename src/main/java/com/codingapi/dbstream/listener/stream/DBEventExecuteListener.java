package com.codingapi.dbstream.listener.stream;

import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import com.codingapi.dbstream.parser.*;
import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.stream.DBEvent;
import com.codingapi.dbstream.stream.TransactionEventPools;

import java.sql.SQLException;
import java.util.List;

/**
 *  数据事件解析监听对象
 */
public abstract class DBEventExecuteListener implements SQLExecuteListener {

    /**
     * SQL适配检测
     */
    public abstract boolean support(String sql);

    /**
     * 构建对应处理的SQL解析对象
     */
    public abstract SQLParser createSQLParser(String sql);

    /**
     * 构建对应处理的事件解析对象
     */
    public abstract DBEventParser createDbEventParser(SQLExecuteState sqlExecuteState,SQLParser sqlParser,DbTable dbTable);

    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        String sql = executeState.getSql();
        if (this.support(sql)) {
            try {
                // 清空执行历史
                ThreadLocalContext.getInstance().remove();
                // 获取SQL解析对象
                SQLParser sqlParser = this.createSQLParser(sql);
                // 提取表明
                String tableName = sqlParser.getTableName();
                // 触发待更新的元数据表信息
                executeState.triggerDBMetaData(tableName);
                // 提取对应元数据表信息
                DbTable dbTable = executeState.getDbTable(tableName);
                // 判断是否支持对该表的DB事件支持
                if (dbTable != null && DBStreamContext.getInstance().support(executeState.getDriverProperties(), dbTable)) {
                    // 是否批量模式判断
                    if (executeState.isBatchMode()) {
                        // 批量模式下，将获取批量的SQL执行结果数据
                        List<SQLExecuteState> executeStateList = executeState.getBatchSQLExecuteStateList();
                        for (int i = 0; i < executeStateList.size(); i++) {
                            SQLExecuteState sqlExecuteState = executeStateList.get(i);
                            DBEventParser dataParser = this.createDbEventParser(sqlExecuteState,sqlParser,dbTable);
                            // DB事件解析前置
                            dataParser.prepare();
                            // 存储到本地线程
                            ThreadLocalContext.getInstance().push(i, dataParser);
                        }
                    } else {
                        // 非批量模式执行
                        DBEventParser dataParser = this.createDbEventParser(executeState,sqlParser,dbTable);
                        // DB事件解析前置
                        dataParser.prepare();
                        // 存储到本地线程
                        ThreadLocalContext.getInstance().push(dataParser);
                    }
                }
            } catch (Exception e) {
                // 异常清空缓存数据
                ThreadLocalContext.getInstance().remove();
                throw new SQLException(e);
            }
        }
    }

    @Override
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        String sql = executeState.getSql();
        // 获取事务标识信息
        String transactionKey = executeState.getTransactionKey();
        if (this.support(sql)) {
            // 批量模式
            if (executeState.isBatchMode()) {
                List<SQLExecuteState> executeStateList = executeState.getBatchSQLExecuteStateList();
                for (int i = 0; i < executeStateList.size(); i++) {
                    DBEventParser dataParser = ThreadLocalContext.getInstance().get(i);
                    if (dataParser != null) {
                        // 获取DB事件信息
                        List<DBEvent> eventList = dataParser.loadEvents(result);
                        TransactionEventPools.getInstance().addEvents(transactionKey, eventList);
                    }
                }
                // 清空本地缓存数据
                ThreadLocalContext.getInstance().remove();
            } else {
                // 非批量模式
                DBEventParser dataParser = ThreadLocalContext.getInstance().get();
                if (dataParser != null) {
                    // 获取DB事件信息
                    List<DBEvent> eventList = dataParser.loadEvents(result);
                    TransactionEventPools.getInstance().addEvents(transactionKey, eventList);
                }
                // 清空本地缓存数据
                ThreadLocalContext.getInstance().remove();
            }
        }
    }

}
