package com.codingapi.dbstream.driver;

import com.codingapi.dbstream.interceptor.SQLRunningContext;
import com.codingapi.dbstream.listener.stream.SQLDeleteExecuteListener;
import com.codingapi.dbstream.listener.stream.SQLInsertExecuteListener;
import com.codingapi.dbstream.listener.stream.SQLUpdateExecuteListener;
import com.codingapi.dbstream.proxy.ConnectionProxy;
import com.codingapi.dbstream.scanner.DBMetaContext;
import com.codingapi.dbstream.scanner.DBMetaData;
import com.codingapi.dbstream.scanner.DBScanner;
import com.codingapi.dbstream.utils.JDBCPropertyUtils;

import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DBStreamProxyDriver implements Driver {

    private static final ConcurrentMap<String, Driver> DRIVER_CACHE = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(DBStreamProxyDriver.class.getName());

    static {
        try {
            DriverManager.registerDriver(new DBStreamProxyDriver());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to register DBStreamProxyDriver", e);
            throw new RuntimeException("Failed to register DBStreamProxyDriver", e);
        }
        SQLRunningContext.getInstance().addListener(new SQLDeleteExecuteListener());
        SQLRunningContext.getInstance().addListener(new SQLInsertExecuteListener());
        SQLRunningContext.getInstance().addListener(new SQLUpdateExecuteListener());
        LOGGER.info("DBStreamProxyDriver initialized and registered");
    }

    /**
     * 查找接受指定 URL 的真实 JDBC 驱动
     *
     * @param url JDBC URL
     * @return 真实的 JDBC 驱动，如果未找到则返回 null
     */
    private Driver findDriver(String url) throws SQLException {
        if (url == null) {
            return null;
        }

        // 从缓存中查找（使用 URL 前缀作为 key）
        Driver cachedDriver = DRIVER_CACHE.get(url);
        if (cachedDriver != null) {
            try {
                // 验证缓存的驱动仍然接受该 URL
                if (cachedDriver.acceptsURL(url)) {
                    return cachedDriver;
                } else {
                    // 如果缓存的驱动不再接受该 URL，从缓存中移除
                    DRIVER_CACHE.remove(url, cachedDriver);
                }
            } catch (SQLException e) {
                // 如果验证失败，从缓存中移除并继续查找
                DRIVER_CACHE.remove(url, cachedDriver);
                LOGGER.log(Level.FINE, "Cached driver no longer accepts URL: " + url, e);
            }
        }

        // 遍历所有已注册的驱动
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().equals(DBStreamProxyDriver.class)) {
                continue;
            }
            try {
                if (driver.acceptsURL(url)) {
                    // 缓存驱动（使用 URL 的前缀作为 key，因为同一个数据库类型的 URL 前缀相同）
                    DRIVER_CACHE.putIfAbsent(url, driver);
                    return driver;
                }
            } catch (SQLException e) {
                // 忽略单个驱动的异常，继续查找
                LOGGER.log(Level.FINE, "Driver " + driver.getClass().getName() + " does not accept URL: " + url, e);
            }
        }
        return null;
    }


    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("URL cannot be null");
        }

        Driver driver = findDriver(url);
        if (driver == null) {
            throw new SQLException("No suitable driver found for " + url);
        }

        Connection connection = driver.connect(url, info);
        if (connection == null) {
            throw new SQLException("Driver returned null connection for URL: " + url);
        }
        info.setProperty(DBMetaData.KEY_JDBC_URL, url);
        String jdbcKey = JDBCPropertyUtils.getJdbcKey(info,connection.getSchema());
        DBMetaData metaData = DBMetaContext.getInstance().getMetaData(jdbcKey);
        if (metaData == null) {
            DBScanner scanner = new DBScanner(connection, info);
            metaData = scanner.loadMetadata();
            DBMetaContext.getInstance().update(metaData);
        }
        return new ConnectionProxy(connection, metaData);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            return false;
        }
        Driver driver = findDriver(url);
        return driver != null;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        Driver driver = findDriver(url);
        if (driver == null) {
            throw new SQLException("No suitable driver found for " + url);
        }
        return driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        // 返回代理驱动的主版本号
        return 1;
    }

    @Override
    public int getMinorVersion() {
        // 返回代理驱动的次版本号
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        // 代理驱动本身不直接兼容 JDBC，它依赖于底层驱动
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return LOGGER;
    }
}
