package com.codingapi.dbstream.provider;

import java.util.Properties;

public class DefaultDBTableSupportProvider implements DBTableSupportProvider {

    @Override
    public boolean support(Properties info, String tableName) {
        return true;
    }
}
