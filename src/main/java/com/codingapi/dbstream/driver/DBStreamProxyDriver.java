package com.codingapi.dbstream.driver;

import com.codingapi.dbstream.interceptor.SQLRunningContext;
import com.codingapi.dbstream.listener.SQLDeleteExecuteListener;
import com.codingapi.dbstream.listener.SQLInsertExecuteListener;
import com.codingapi.dbstream.listener.SQLUpdateExecuteListener;
import com.codingapi.dbstream.proxy.ConnectionProxy;
import com.codingapi.dbstream.scanner.DBMetaContext;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DBScanner;

import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class DBStreamProxyDriver implements Driver {

    private Driver driver;

    static {
        SQLRunningContext.getInstance().addListener(new SQLDeleteExecuteListener());
        SQLRunningContext.getInstance().addListener(new SQLInsertExecuteListener());
        SQLRunningContext.getInstance().addListener(new SQLUpdateExecuteListener());
        System.out.println("DBStreamProxyDriver init...");
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection connection = driver.connect(url, info);
        info.setProperty(DBMetaData.KEY_JDBC_URL, url);
        String jdbcKey = info.getProperty(DBMetaData.KEY_JDBC_KEY);
        DBMetaData metaData = DBMetaContext.getInstance().getMetaData(jdbcKey);
        if (metaData == null) {
            DBScanner scanner = new DBScanner(connection);
            metaData = scanner.loadMetadata(info);
            DBMetaContext.getInstance().update(metaData);
        }
        return new ConnectionProxy(connection, metaData);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.acceptsURL(url)) {
                this.driver = driver;
                return true;
            }
        }
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return driver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }
}
