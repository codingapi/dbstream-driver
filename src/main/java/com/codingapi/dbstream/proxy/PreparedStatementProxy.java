package com.codingapi.dbstream.proxy;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.interceptor.SQLRunningContext;
import com.codingapi.dbstream.scanner.DBMetaData;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

public class PreparedStatementProxy implements PreparedStatement {

    private final PreparedStatement preparedStatement;
    private final DBMetaData metaData;
    private final SQLExecuteState executeState;

    public PreparedStatementProxy(ConnectionProxy connection, PreparedStatement preparedStatement, DBMetaData metaData, String sql) throws SQLException {
        this.preparedStatement = preparedStatement;
        this.metaData = metaData;
        this.executeState = new SQLExecuteState(sql, connection, this, metaData);
        this.cachedKeys = null;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        SQLRunningContext.getInstance().before(this.executeState);
        ResultSet resultSet = preparedStatement.executeQuery();
        SQLRunningContext.getInstance().after(this.executeState, resultSet);
        return resultSet;
    }

    @Override
    public int executeUpdate() throws SQLException {
        SQLRunningContext.getInstance().before(this.executeState);
        int result = preparedStatement.executeUpdate();
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        preparedStatement.setNull(parameterIndex, sqlType);
        this.executeState.setParam(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        preparedStatement.setBoolean(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        preparedStatement.setByte(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        preparedStatement.setShort(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        preparedStatement.setInt(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        preparedStatement.setLong(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        preparedStatement.setFloat(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        preparedStatement.setDouble(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        preparedStatement.setBigDecimal(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        preparedStatement.setString(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        preparedStatement.setBytes(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        preparedStatement.setDate(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        preparedStatement.setTime(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        preparedStatement.setTimestamp(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void clearParameters() throws SQLException {
        preparedStatement.clearParameters();
        this.executeState.cleanParams();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        preparedStatement.setObject(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        SQLRunningContext.getInstance().before(this.executeState);
        boolean result = preparedStatement.execute();
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
        this.executeState.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        preparedStatement.setRef(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        preparedStatement.setBlob(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        preparedStatement.setClob(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        preparedStatement.setArray(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        preparedStatement.setDate(parameterIndex, x, cal);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        preparedStatement.setTime(parameterIndex, x, cal);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        preparedStatement.setTimestamp(parameterIndex, x, cal);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        preparedStatement.setNull(parameterIndex, sqlType, typeName);
        this.executeState.setParam(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        preparedStatement.setURL(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        preparedStatement.setRowId(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        preparedStatement.setNString(parameterIndex, value);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        preparedStatement.setNCharacterStream(parameterIndex, value, length);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        preparedStatement.setNClob(parameterIndex, value);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatement.setClob(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        preparedStatement.setBlob(parameterIndex, inputStream, length);
        this.executeState.setParam(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatement.setNClob(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        preparedStatement.setSQLXML(parameterIndex, xmlObject);
        this.executeState.setParam(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        preparedStatement.setAsciiStream(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        preparedStatement.setBinaryStream(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        preparedStatement.setCharacterStream(parameterIndex, reader);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        preparedStatement.setNCharacterStream(parameterIndex, value);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        preparedStatement.setClob(parameterIndex, reader);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        preparedStatement.setBlob(parameterIndex, inputStream);
        this.executeState.setParam(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        preparedStatement.setNClob(parameterIndex, reader);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        SQLRunningContext.getInstance().before(this.executeState);
        long result = preparedStatement.executeLargeUpdate();
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        ResultSet resultSet = preparedStatement.executeQuery(sql);
        SQLRunningContext.getInstance().after(this.executeState, resultSet);
        return resultSet;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        int result = preparedStatement.executeUpdate(sql);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }


    @Override
    public int getMaxFieldSize() throws SQLException {
        return preparedStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        preparedStatement.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return preparedStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        preparedStatement.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        preparedStatement.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return preparedStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        preparedStatement.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        preparedStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return preparedStatement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        preparedStatement.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        preparedStatement.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        boolean result = preparedStatement.execute(executeState.getSql());
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return preparedStatement.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return preparedStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return preparedStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        preparedStatement.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return preparedStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        preparedStatement.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return preparedStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return preparedStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return preparedStatement.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        preparedStatement.addBatch(sql);
        this.executeState.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        preparedStatement.clearBatch();
        this.executeState.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        SQLRunningContext.getInstance().before(this.executeState);
        int[] result = preparedStatement.executeBatch();
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ConnectionProxy(preparedStatement.getConnection(), this.metaData);
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return preparedStatement.getMoreResults(current);
    }

    private CachedRowSet cachedKeys;

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        if (cachedKeys != null) {
            cachedKeys.beforeFirst();
            return cachedKeys;
        }
        try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
            CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
            rowSet.populate(rs);
            cachedKeys = rowSet;
            return cachedKeys;
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            if (cachedKeys != null) {
                cachedKeys.close();
                cachedKeys = null;
            }
        } finally {
            preparedStatement.close();
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        int result = preparedStatement.executeUpdate(executeState.getSql(), autoGeneratedKeys);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        int result = preparedStatement.executeUpdate(executeState.getSql(), columnIndexes);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        int result = preparedStatement.executeUpdate(sql, columnNames);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        boolean result = preparedStatement.execute(sql, autoGeneratedKeys);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        boolean result = preparedStatement.execute(sql, columnIndexes);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        boolean result = preparedStatement.execute(sql, columnNames);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return preparedStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return preparedStatement.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        preparedStatement.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return preparedStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        preparedStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return preparedStatement.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return preparedStatement.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        preparedStatement.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return preparedStatement.getLargeMaxRows();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        SQLRunningContext.getInstance().before(this.executeState);
        long[] result = preparedStatement.executeLargeBatch();
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        long result = preparedStatement.executeLargeUpdate(sql);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        long result = preparedStatement.executeLargeUpdate(sql, autoGeneratedKeys);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        long result = preparedStatement.executeLargeUpdate(sql, columnIndexes);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        this.executeState.setSql(sql);
        SQLRunningContext.getInstance().before(this.executeState);
        long result = preparedStatement.executeLargeUpdate(sql, columnNames);
        SQLRunningContext.getInstance().after(this.executeState, result);
        return result;

    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return preparedStatement.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return preparedStatement.isWrapperFor(iface);
    }
}
