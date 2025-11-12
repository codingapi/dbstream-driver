package com.codingapi.dbstream.listener;

import java.sql.SQLException;

/**
 * SQL执行监听器
 */
public interface SQLExecuteListener {

    /**
     * 执行顺序，越小越靠前
     *
     * @return index
     */
    int order();

    /**
     * before sql execute
     *
     * @param runningState execute state content
     * @throws SQLException throws {@link SQLException}
     */
    void before(SQLRunningState runningState) throws SQLException;

    /**
     * after sql execute
     *
     * @param runningState execute state content
     * @throws SQLException throws {@link SQLException}
     */
    void after(SQLRunningState runningState, Object result) throws SQLException;

}
