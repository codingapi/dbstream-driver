package com.codingapi.dbstream.provider;

import com.codingapi.dbstream.scanner.DbTable;

import java.util.Properties;

public class DefaultDBTableSupportProvider implements DBTableSupportProvider {

    @Override
    public boolean support(Properties info, DbTable dbTable) {
        return true;
    }
}
