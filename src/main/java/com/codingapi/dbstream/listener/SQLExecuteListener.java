package com.codingapi.dbstream.listener;

import com.codingapi.dbstream.interceptor.SQLExecuteState;

import java.sql.SQLException;

public interface SQLExecuteListener {

    void after(SQLExecuteState executeState, Object result) throws SQLException;

    void before(SQLExecuteState executeState) throws SQLException;
}
