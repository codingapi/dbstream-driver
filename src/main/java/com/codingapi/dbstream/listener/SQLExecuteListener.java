package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.interceptor.SQLExecuteState;

import java.sql.SQLException;

public interface SQLExecuteListener {

    /**
     * before sql execute
     *
     * @param executeState execute state content
     * @throws SQLException throws {@link SQLException}
     */
    void before(SQLExecuteState executeState) throws SQLException;

    /**
     * after sql execute
     *
     * @param executeState execute state content
     * @throws SQLException throws {@link SQLException}
     */
    void after(SQLExecuteState executeState, Object result) throws SQLException;

}
