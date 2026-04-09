package com.codingapi.dbstream.scanner;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class JavaDBTypeConvertor {

    private static final Map<Integer, Class<?>> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(Types.INTEGER, Integer.class);
        TYPE_MAP.put(Types.BIGINT, Long.class);
        TYPE_MAP.put(Types.SMALLINT, Short.class);
        TYPE_MAP.put(Types.TINYINT, Byte.class);

        TYPE_MAP.put(Types.FLOAT, Float.class);
        TYPE_MAP.put(Types.REAL, Float.class);
        TYPE_MAP.put(Types.DOUBLE, Double.class);

        TYPE_MAP.put(Types.NUMERIC, BigDecimal.class);
        TYPE_MAP.put(Types.DECIMAL, BigDecimal.class);

        TYPE_MAP.put(Types.CHAR, String.class);
        TYPE_MAP.put(Types.VARCHAR, String.class);
        TYPE_MAP.put(Types.LONGVARCHAR, String.class);

        TYPE_MAP.put(Types.DATE, java.sql.Date.class);
        TYPE_MAP.put(Types.TIME, java.sql.Time.class);
        TYPE_MAP.put(Types.TIMESTAMP, java.sql.Timestamp.class);

        TYPE_MAP.put(Types.BOOLEAN, Boolean.class);
        TYPE_MAP.put(Types.BIT, Boolean.class);

        TYPE_MAP.put(Types.BINARY, byte[].class);
        TYPE_MAP.put(Types.VARBINARY, byte[].class);
        TYPE_MAP.put(Types.LONGVARBINARY, byte[].class);

        TYPE_MAP.put(Types.BLOB, java.sql.Blob.class);
        TYPE_MAP.put(Types.CLOB, java.sql.Clob.class);
    }

    public static Class<?> toJavaType(int jdbcType) {
        return TYPE_MAP.getOrDefault(jdbcType, Object.class);
    }
}
