package com.codingapi.dbstream.supporter;

import com.codingapi.dbstream.scanner.DbTable;

import java.util.Properties;

/**
 * 数据库表支持判断
 */
public interface DBEventSupporter {

    /**
     * 是否支持该表的DB事件分析
     */
    boolean support(Properties info, DbTable dbTable);

}
