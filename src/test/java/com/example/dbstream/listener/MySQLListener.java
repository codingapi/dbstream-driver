package com.example.dbstream.listener;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class MySQLListener implements SQLExecuteListener {

    @Override
    public void after(SQLExecuteState executeState, Object result) throws SQLException {
        log.info("after sql:{},params:{},execute timestamp:{}", executeState.getSql(), executeState.getListParams(), executeState.getExecuteTimestamp());
    }

    @Override
    public void before(SQLExecuteState executeState) throws SQLException {
        log.info("before sql:{},params:{}", executeState.getSql(), executeState.getListParams());
    }
}
