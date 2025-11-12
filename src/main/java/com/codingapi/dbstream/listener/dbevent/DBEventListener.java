package com.codingapi.dbstream.listener.dbevent;

import com.codingapi.dbstream.DBStreamContext;
import com.codingapi.dbstream.event.DBEvent;
import com.codingapi.dbstream.event.TransactionEventPools;
import com.codingapi.dbstream.listener.SQLRunningState;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.scanner.DbTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据事件解析监听对象
 */
public abstract class DBEventListener implements SQLExecuteListener {

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
    public abstract DBEventParser createDbEventParser(SQLRunningState runningState, SQLParser sqlParser, DbTable dbTable);

    @Override
    public void before(SQLRunningState runningState) throws SQLException {
        String sql = runningState.getSql();
        if (this.support(sql)) {
            try {
                // 清空执行历史
                DBEventCacheContext.getInstance().remove();
                // 获取SQL解析对象
                SQLParser sqlParser = this.createSQLParser(sql);
                // 提取表明
                String tableName = sqlParser.getTableName();
                // 触发待更新的元数据表信息
                runningState.triggerDBMetaData(tableName);
                // 提取对应元数据表信息
                DbTable dbTable = runningState.getDbTable(tableName);
                // 判断是否支持对该表的DB事件支持
                if (dbTable != null && DBStreamContext.getInstance().support(runningState.getDriverProperties(), dbTable)) {
                    // 是否批量模式判断
                    if (runningState.isBatchMode()) {
                        // 批量模式下，将获取批量的SQL执行结果数据
                        List<SQLRunningState> runningStateList = runningState.getBatchSQLRunningStateList();
                        for (int i = 0; i < runningStateList.size(); i++) {
                            SQLRunningState sqlRunningState = runningStateList.get(i);
                            DBEventParser dataParser = this.createDbEventParser(sqlRunningState, sqlParser, dbTable);
                            // DB事件解析前置
                            dataParser.prepare();
                            // 存储到本地线程
                            DBEventCacheContext.getInstance().push(i, dataParser);
                        }
                    } else {
                        // 非批量模式执行
                        DBEventParser dataParser = this.createDbEventParser(runningState, sqlParser, dbTable);
                        // DB事件解析前置
                        dataParser.prepare();
                        // 存储到本地线程
                        DBEventCacheContext.getInstance().push(dataParser);
                    }
                }
            } catch (Exception e) {
                // 异常清空缓存数据
                DBEventCacheContext.getInstance().remove();
                throw new SQLException(e);
            }
        }
    }


    /**
     * batch result 结果数组转化
     */
    private List<Object> batchResultToArrays(Object result, int size) {
        List<Object> list = new ArrayList<>();
        if (result instanceof int[]) {
            int[] rows = (int[]) result;
            for (int row : rows) {
                list.add(row);
            }
            return list;
        }
        if (result instanceof long[]) {
            long[] rows = (long[]) result;
            for (long row : rows) {
                list.add(row);
            }
            return list;
        }

        // 如果非int[] 和 long[] 的返回数据，则直接返回0，忽略事件
        for (int i = 0; i < size; i++) {
            list.add(0);
        }
        return list;

    }

    @Override
    public void after(SQLRunningState runningState, Object result) throws SQLException {
        String sql = runningState.getSql();
        // 获取事务标识信息
        String transactionKey = runningState.getTransactionKey();
        if (this.support(sql)) {
            // 批量模式
            if (runningState.isBatchMode()) {
                List<SQLRunningState> runningStateList = runningState.getBatchSQLRunningStateList();
                int batchSize = runningStateList.size();

                //批量模式下的返回数据是数组格式
                List<Object> arrays = this.batchResultToArrays(result, batchSize);

                for (int i = 0; i < batchSize; i++) {
                    DBEventParser dataParser = DBEventCacheContext.getInstance().get(i);
                    SQLRunningState sqlRunningState = runningStateList.get(i);
                    if (dataParser != null) {
                        // 获取DB事件信息
                        List<DBEvent> eventList = dataParser.loadEvents(arrays.get(i));
                        TransactionEventPools.getInstance().addEvents(sqlRunningState.getJdbcQuery(), transactionKey, eventList);
                    }
                }
                // 清空本地缓存数据
                DBEventCacheContext.getInstance().remove();
            } else {
                // 非批量模式
                DBEventParser dataParser = DBEventCacheContext.getInstance().get();
                if (dataParser != null) {
                    // 获取DB事件信息
                    List<DBEvent> eventList = dataParser.loadEvents(result);
                    TransactionEventPools.getInstance().addEvents(runningState.getJdbcQuery(), transactionKey, eventList);
                }
                // 清空本地缓存数据
                DBEventCacheContext.getInstance().remove();
            }
        }
    }

}
