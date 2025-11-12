package com.codingapi.dbstream.proxy;

import com.codingapi.dbstream.interceptor.SQLExecuteState;
import com.codingapi.dbstream.interceptor.SQLExecuteListenerContext;
import com.codingapi.dbstream.scanner.DBMetaData;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class CallableStatementProxy implements CallableStatement {

    private final CallableStatement callableStatement;
    private final DBMetaData metaData;
    private final SQLExecuteState executeState;

    public CallableStatementProxy(ConnectionProxy connection, CallableStatement callableStatement, DBMetaData metaData, String sql) throws SQLException {
        this.callableStatement = callableStatement;
        this.metaData = metaData;
        this.executeState = new SQLExecuteState(sql, connection, this, metaData);
        this.cachedKeys = null;
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, scale);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return callableStatement.wasNull();
    }

    @Override
    public String getString(int parameterIndex) throws SQLException {
        return callableStatement.getString(parameterIndex);
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        return callableStatement.getBoolean(parameterIndex);
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        return callableStatement.getByte(parameterIndex);
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        return callableStatement.getShort(parameterIndex);
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        return callableStatement.getInt(parameterIndex);
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        return callableStatement.getLong(parameterIndex);
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        return callableStatement.getFloat(parameterIndex);
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        return callableStatement.getDouble(parameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex, scale);
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        return callableStatement.getBytes(parameterIndex);
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        return callableStatement.getDate(parameterIndex);
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        return callableStatement.getTime(parameterIndex);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return callableStatement.getTimestamp(parameterIndex);
    }

    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        return callableStatement.getObject(parameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex);
    }

    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        return callableStatement.getObject(parameterIndex, map);
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        return callableStatement.getRef(parameterIndex);
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        return callableStatement.getBlob(parameterIndex);
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        return callableStatement.getClob(parameterIndex);
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        return callableStatement.getArray(parameterIndex);
    }

    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return callableStatement.getDate(parameterIndex, cal);
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return callableStatement.getTime(parameterIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return callableStatement.getTimestamp(parameterIndex, cal);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, typeName);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType, scale);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType, typeName);
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        return callableStatement.getURL(parameterIndex);
    }

    @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        callableStatement.setURL(parameterName, val);
        this.executeState.setParam(parameterName, val);
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        callableStatement.setNull(parameterName, sqlType);
        this.executeState.setParam(parameterName, null);
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        callableStatement.setBoolean(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        callableStatement.setByte(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        callableStatement.setShort(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        callableStatement.setInt(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        callableStatement.setLong(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        callableStatement.setFloat(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        callableStatement.setDouble(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        callableStatement.setBigDecimal(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        callableStatement.setString(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        callableStatement.setBytes(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        callableStatement.setDate(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        callableStatement.setTime(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        callableStatement.setTimestamp(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        callableStatement.setAsciiStream(parameterName, x, length);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        callableStatement.setBinaryStream(parameterName, x, length);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        callableStatement.setObject(parameterName, x, targetSqlType, scale);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        callableStatement.setObject(parameterName, x, targetSqlType);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        callableStatement.setObject(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        callableStatement.setCharacterStream(parameterName, reader, length);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        callableStatement.setDate(parameterName, x, cal);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        callableStatement.setTime(parameterName, x, cal);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        callableStatement.setTimestamp(parameterName, x, cal);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        callableStatement.setNull(parameterName, sqlType, typeName);
        this.executeState.setParam(parameterName, null);
    }

    @Override
    public String getString(String parameterName) throws SQLException {
        return callableStatement.getString(parameterName);
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        return callableStatement.getBoolean(parameterName);
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        return callableStatement.getByte(parameterName);
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        return callableStatement.getShort(parameterName);
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        return callableStatement.getInt(parameterName);
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        return callableStatement.getLong(parameterName);
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        return callableStatement.getFloat(parameterName);
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        return callableStatement.getDouble(parameterName);
    }

    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        return callableStatement.getBytes(parameterName);
    }

    @Override
    public Date getDate(String parameterName) throws SQLException {
        return callableStatement.getDate(parameterName);
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        return callableStatement.getTime(parameterName);
    }

    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return callableStatement.getTimestamp(parameterName);
    }

    @Override
    public Object getObject(String parameterName) throws SQLException {
        return callableStatement.getObject(parameterName);
    }

    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return callableStatement.getBigDecimal(parameterName);
    }

    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return callableStatement.getObject(parameterName, map);
    }

    @Override
    public Ref getRef(String parameterName) throws SQLException {
        return callableStatement.getRef(parameterName);
    }

    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        return callableStatement.getBlob(parameterName);
    }

    @Override
    public Clob getClob(String parameterName) throws SQLException {
        return callableStatement.getClob(parameterName);
    }

    @Override
    public Array getArray(String parameterName) throws SQLException {
        return callableStatement.getArray(parameterName);
    }

    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return callableStatement.getDate(parameterName, cal);
    }

    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return callableStatement.getTime(parameterName, cal);
    }

    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return callableStatement.getTimestamp(parameterName, cal);
    }

    @Override
    public URL getURL(String parameterName) throws SQLException {
        return callableStatement.getURL(parameterName);
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        return callableStatement.getRowId(parameterIndex);
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        return callableStatement.getRowId(parameterName);
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        callableStatement.setRowId(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        callableStatement.setNString(parameterName, value);
        this.executeState.setParam(parameterName, value);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        callableStatement.setNCharacterStream(parameterName, value, length);
        this.executeState.setParam(parameterName, value);
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        callableStatement.setNClob(parameterName, value);
        this.executeState.setParam(parameterName, value);
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        callableStatement.setClob(parameterName, reader, length);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        callableStatement.setBlob(parameterName, inputStream, length);
        this.executeState.setParam(parameterName, inputStream);
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        callableStatement.setNClob(parameterName, reader, length);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        return callableStatement.getNClob(parameterIndex);
    }

    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        return callableStatement.getNClob(parameterName);
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        callableStatement.setSQLXML(parameterName, xmlObject);
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return callableStatement.getSQLXML(parameterIndex);
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return callableStatement.getSQLXML(parameterName);
    }

    @Override
    public String getNString(int parameterIndex) throws SQLException {
        return callableStatement.getNString(parameterIndex);
    }

    @Override
    public String getNString(String parameterName) throws SQLException {
        return callableStatement.getNString(parameterName);
    }

    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return callableStatement.getNCharacterStream(parameterIndex);
    }

    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return callableStatement.getNCharacterStream(parameterName);
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return callableStatement.getCharacterStream(parameterIndex);
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        return callableStatement.getCharacterStream(parameterName);
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        callableStatement.setBlob(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        callableStatement.setClob(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        callableStatement.setAsciiStream(parameterName, x, length);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        callableStatement.setBinaryStream(parameterName, x, length);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        callableStatement.setCharacterStream(parameterName, reader, length);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        callableStatement.setAsciiStream(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        callableStatement.setBinaryStream(parameterName, x);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        callableStatement.setCharacterStream(parameterName, reader);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        callableStatement.setNCharacterStream(parameterName, value);
        this.executeState.setParam(parameterName, value);
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        callableStatement.setClob(parameterName, reader);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        callableStatement.setBlob(parameterName, inputStream);
        this.executeState.setParam(parameterName, inputStream);
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        callableStatement.setNClob(parameterName, reader);
        this.executeState.setParam(parameterName, reader);
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return callableStatement.getObject(parameterIndex, type);
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return callableStatement.getObject(parameterName, type);
    }

    @Override
    public void setObject(String parameterName, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        callableStatement.setObject(parameterName, x, targetSqlType, scaleOrLength);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void setObject(String parameterName, Object x, SQLType targetSqlType) throws SQLException {
        callableStatement.setObject(parameterName, x, targetSqlType);
        this.executeState.setParam(parameterName, x);
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType);
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, scale);
    }

    @Override
    public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, typeName);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType, scale);
    }

    @Override
    public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
        callableStatement.registerOutParameter(parameterName, sqlType, typeName);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        ResultSet resultSet = callableStatement.executeQuery();
        SQLExecuteListenerContext.getInstance().after(this.executeState, resultSet);
        return resultSet;
    }

    @Override
    public int executeUpdate() throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        int result = callableStatement.executeUpdate();
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        callableStatement.setNull(parameterIndex, sqlType);
        this.executeState.setParam(parameterIndex, null);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        callableStatement.setBoolean(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        callableStatement.setByte(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        callableStatement.setShort(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        callableStatement.setInt(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        callableStatement.setLong(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        callableStatement.setFloat(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        callableStatement.setDouble(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        callableStatement.setBigDecimal(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        callableStatement.setString(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        callableStatement.setBytes(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        callableStatement.setDate(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        callableStatement.setTime(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        callableStatement.setTimestamp(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        callableStatement.setAsciiStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        callableStatement.setUnicodeStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        callableStatement.setBinaryStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void clearParameters() throws SQLException {
        callableStatement.clearParameters();
        this.executeState.cleanParams();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        callableStatement.setObject(parameterIndex, x, targetSqlType);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        callableStatement.setObject(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        boolean result = callableStatement.execute();
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public void addBatch() throws SQLException {
        callableStatement.addBatch();
        this.executeState.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        callableStatement.setCharacterStream(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        callableStatement.setRef(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        callableStatement.setBlob(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        callableStatement.setClob(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        callableStatement.setArray(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return callableStatement.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        callableStatement.setDate(parameterIndex, x, cal);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        callableStatement.setTime(parameterIndex, x, cal);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        callableStatement.setTimestamp(parameterIndex, x, cal);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        callableStatement.setNull(parameterIndex, sqlType, typeName);
        this.executeState.setParam(parameterIndex, null);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        callableStatement.setURL(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return callableStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        callableStatement.setRowId(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        callableStatement.setNString(parameterIndex, value);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        callableStatement.setNCharacterStream(parameterIndex, value, length);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        callableStatement.setNClob(parameterIndex, value);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        callableStatement.setClob(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        callableStatement.setBlob(parameterIndex, inputStream, length);
        this.executeState.setParam(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        callableStatement.setNClob(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        callableStatement.setSQLXML(parameterIndex, xmlObject);
        this.executeState.setParam(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        callableStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        callableStatement.setAsciiStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        callableStatement.setBinaryStream(parameterIndex, x, length);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        callableStatement.setCharacterStream(parameterIndex, reader, length);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        callableStatement.setAsciiStream(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        callableStatement.setBinaryStream(parameterIndex, x);
        this.executeState.setParam(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        callableStatement.setCharacterStream(parameterIndex, reader);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        callableStatement.setNCharacterStream(parameterIndex, value);
        this.executeState.setParam(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        callableStatement.setClob(parameterIndex, reader);
        this.executeState.setParam(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        callableStatement.setBlob(parameterIndex, inputStream);
        this.executeState.setParam(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        callableStatement.setNClob(parameterIndex, reader);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        callableStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        callableStatement.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return callableStatement.executeLargeUpdate();
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        ResultSet resultSet = callableStatement.executeQuery(sql);
        SQLExecuteListenerContext.getInstance().after(this.executeState, resultSet);
        return resultSet;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        int result = callableStatement.executeUpdate(sql);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }


    @Override
    public int getMaxFieldSize() throws SQLException {
        return callableStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        callableStatement.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return callableStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        callableStatement.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        callableStatement.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return callableStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        callableStatement.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        callableStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return callableStatement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        callableStatement.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        callableStatement.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        boolean result = callableStatement.execute(sql);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return callableStatement.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return callableStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return callableStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        callableStatement.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return callableStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        callableStatement.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return callableStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return callableStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return callableStatement.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        callableStatement.addBatch(sql);
        this.executeState.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        callableStatement.clearBatch();
        this.executeState.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        int[] result = callableStatement.executeBatch();
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ConnectionProxy(callableStatement.getConnection(), this.metaData);
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return callableStatement.getMoreResults(current);
    }


    private CachedRowSet cachedKeys;

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        if (cachedKeys != null) {
            cachedKeys.beforeFirst();
            return cachedKeys;
        }
        try (ResultSet rs = callableStatement.getGeneratedKeys()) {
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
            callableStatement.close();
        }
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        int result = callableStatement.executeUpdate(sql, autoGeneratedKeys);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        int result = callableStatement.executeUpdate(sql, columnIndexes);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        int result = callableStatement.executeUpdate(sql, columnNames);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        boolean result = callableStatement.execute(sql, autoGeneratedKeys);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        boolean result = callableStatement.execute(sql, columnIndexes);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        boolean result = callableStatement.execute(sql, columnNames);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return callableStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return callableStatement.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        callableStatement.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return callableStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        callableStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return callableStatement.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return callableStatement.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        callableStatement.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return callableStatement.getLargeMaxRows();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        long[] result = callableStatement.executeLargeBatch();
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        long result = callableStatement.executeLargeUpdate(sql);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        long result = callableStatement.executeLargeUpdate(sql, autoGeneratedKeys);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        long result = callableStatement.executeLargeUpdate(sql, columnIndexes);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        this.executeState.setSql(sql);
        SQLExecuteListenerContext.getInstance().before(this.executeState);
        long result = callableStatement.executeLargeUpdate(sql, columnNames);
        SQLExecuteListenerContext.getInstance().after(this.executeState, result);
        return result;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return callableStatement.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return callableStatement.isWrapperFor(iface);
    }
}
