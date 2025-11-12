package com.codingapi.dbstream.supporter;

import com.codingapi.dbstream.scanner.DbTable;

import java.util.Properties;

/**
 * 默认DB事件判断类
 * 规则为满足条件的全部支持
 */
public class DefaultDBEventSupporter implements DBEventSupporter {

    @Override
    public boolean support(Properties info, DbTable dbTable) {
        return true;
    }
}
