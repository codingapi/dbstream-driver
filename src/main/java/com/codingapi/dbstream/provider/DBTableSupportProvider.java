package com.codingapi.dbstream.provider;

import java.util.Properties;

/**
 * 数据库表支持判断
 */
public interface DBTableSupportProvider {

    boolean support(Properties info,String tableName);
}
