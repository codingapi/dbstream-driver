package com.codingapi.dbstream.provider;

import com.codingapi.dbstream.scanner.DbTable;

import java.util.Properties;

/**
 * 数据库表支持判断
 */
public interface DBTableSupportProvider {

    boolean support(Properties info, DbTable dbTable);
}
