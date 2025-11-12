package com.example.dbstream.listener;

import com.codingapi.dbstream.listener.SQLRunningState;
import com.codingapi.dbstream.listener.SQLExecuteListener;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class MySQLListener implements SQLExecuteListener {

    @Override
    public int order() {
        return 0;
    }

    @Override
    public void after(SQLRunningState runningState, Object result) throws SQLException {
        log.info("after sql:{},params:{},execute timestamp:{}", runningState.getSql(), runningState.getListParams(), runningState.getExecuteTimestamp());
    }

    @Override
    public void before(SQLRunningState runningState) throws SQLException {
        log.info("before sql:{},params:{}", runningState.getSql(), runningState.getListParams());
    }
}
