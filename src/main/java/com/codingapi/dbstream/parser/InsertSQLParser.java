package com.codingapi.dbstream.parser;

import com.codingapi.dbstream.scanner.DbColumn;
import com.codingapi.dbstream.utils.SQLUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertSQLParser implements SQLParser {

    // 匹配 INSERT INTO table_name (...)
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
            "(?i)INSERT\\s+INTO\\s+([`\"\\[]?[\\w.]+[`\"\\]]?)",
            Pattern.CASE_INSENSITIVE
    );

    // 匹配列部分：(col1, col2, col3)
    private static final Pattern COLUMNS_PATTERN = Pattern.compile(
            "\\(([^)]+)\\)\\s*(?i)(VALUES|SELECT)"
    );

    private final String sql;

    public InsertSQLParser(String sql) {
        this.sql = SQLUtils.normalize(sql);
    }

    /**
     * 提取表名
     * 示例: INSERT INTO `user` (id,name) VALUES (?,?)  → user
     */
    @Override
    public String getTableName() {
        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        if (matcher.find()) {
            return SQLUtils.stripQuotes(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取插入的字段名称
     * 示例: INSERT INTO user (id,name,age) VALUES (?,?,?)
     * 输出: ["id", "name", "age"]
     */
    public List<String> getColumnValues() {
        Matcher matcher = COLUMNS_PATTERN.matcher(sql);
        if (matcher.find()) {
            String cols = matcher.group(1);
            String[] parts = cols.split(",");
            List<String> columns = new ArrayList<>();
            for (String col : parts) {
                col = SQLUtils.stripQuotes(col.trim());
                if (!col.isEmpty()) {
                    columns.add(col);
                }
            }
            return columns;
        }
        return new ArrayList<>();
    }

    /**
     * 是否为默认的insert语句类型
     * INSERT INTO user (id,name) VALUES (?,?)
     */
    public boolean isDefaultInsertSQL() {
        Pattern valuesPattern = Pattern.compile("(?i)\\b(VALUES)\\b\\s*(.*)$", Pattern.DOTALL);
        Matcher valuesMatcher = valuesPattern.matcher(sql);
        return valuesMatcher.find();
    }

    /**
     * 是否为批量的insert语句类型
     * INSERT INTO user (id,name) VALUES (?,?),(?,?)
     */
    public boolean isBatchInsertSQL() {
        if (!isDefaultInsertSQL()) {
            return false;
        }

        String valuesSQL = getValuesSQL();
        if (valuesSQL == null) {
            return false;
        }

        String normalized = valuesSQL.replaceAll("\\s+", "");
        return normalized.contains("),(");
    }


    /**
     * 提取 VALUES 或 SELECT 后面的 SQL 内容（包含完整结构）
     * 示例:
     * INSERT INTO user (id,name) SELECT id,name FROM other
     * → SELECT id,name FROM other
     * <p>
     * INSERT INTO user (id,name) VALUES (?,?)
     * → VALUES (?,?)
     */
    public String getValuesSQL() {

        // 如果没有匹配到 SELECT，则尝试匹配 VALUES 部分
        Pattern valuesPattern = Pattern.compile("(?i)\\b(VALUES)\\b\\s*(.*)$", Pattern.DOTALL);
        Matcher valuesMatcher = valuesPattern.matcher(sql);
        if (valuesMatcher.find()) {
            return SQLUtils.stripQuotes(valuesMatcher.group(2).trim());
        }

        // 首先尝试匹配 SELECT 部分
        Pattern selectPattern = Pattern.compile("(?i)\\b(SELECT)\\b\\s+(.*)$", Pattern.DOTALL);
        Matcher selectMatcher = selectPattern.matcher(sql);
        if (selectMatcher.find()) {
            String keyword = selectMatcher.group(1).toUpperCase();
            String rest = selectMatcher.group(2).trim();
            return keyword + " " + rest;
        }

        return null;
    }

    private List<String> splitValueGroups(String input) {
        List<String> groups = new ArrayList<>();

        int level = 0;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '(') {
                if (level > 0) {
                    current.append(c);
                }
                level++;
            } else if (c == ')') {
                level--;
                if (level > 0) {
                    current.append(c);
                } else {
                    // 一个完整 group
                    groups.add("(" + current.toString() + ")");
                    current.setLength(0);
                }
            } else if (c == ',' && level == 0) {
                // group 之间的逗号，忽略
                continue;
            } else {
                if (level > 0) {
                    current.append(c);
                }
            }
        }

        return groups;
    }

    public List<List<InsertValue>> getBatchValues() {
        List<List<InsertValue>> result = new ArrayList<>();

        String valuesSQL = getValuesSQL();
        if (valuesSQL == null) {
            return result;
        }

        // 去掉 VALUES 关键字
        String normalized = valuesSQL.trim();
        // 如果没有以 ( 开头，补一个
        if (!normalized.startsWith("(")) {
            normalized = "(" + normalized;
        }

        // 如果没有以 ) 结尾，补一个
        if (!normalized.endsWith(")")) {
            normalized = normalized + ")";
        }

        List<String> groups = splitValueGroups(normalized);

        int jdbcIndex = 0;

        for (String group : groups) {
            // 去掉外层括号
            String inner = group.trim();
            if (inner.startsWith("(") && inner.endsWith(")")) {
                inner = inner.substring(1, inner.length() - 1);
            }

            List<String> values = SQLUtils.parseInsertSQLValues(inner);
            List<InsertValue> row = new ArrayList<>();

            for (String value : values) {
                InsertValue insertValue = new InsertValue();

                String v = value.trim();
                if ("?".equals(v)) {
                    insertValue.setType(ValueType.JDBC);
                    jdbcIndex++;
                    insertValue.setValue("?" + jdbcIndex);
                } else if (SQLUtils.isSQLKeyword(v) || v.startsWith("(")) {
                    insertValue.setType(ValueType.SELECT);
                    insertValue.setValue(v);
                } else {
                    insertValue.setType(ValueType.STATIC);
                    insertValue.setValue(v);
                }

                row.add(insertValue);
            }

            result.add(row);
        }

        return result;
    }

    public List<InsertValue> getValues() {
        List<InsertValue> insertValues = new ArrayList<>();
        List<String> values = SQLUtils.parseInsertSQLValues(this.getValuesSQL());
        int jdbcIndex = 0;
        for (String value : values) {
            InsertValue insertValue = new InsertValue();
            if (value.trim().equals("?")) {
                insertValue.setType(ValueType.JDBC);
                jdbcIndex++;
                insertValue.setValue(value + jdbcIndex);
            } else if (SQLUtils.isSQLKeyword(value.trim()) || value.trim().startsWith("(")) {
                insertValue.setType(ValueType.SELECT);
                insertValue.setValue(value);
            } else {
                insertValue.setType(ValueType.STATIC);
                insertValue.setValue(value);
            }
            insertValues.add(insertValue);
        }
        return insertValues;
    }

    @Setter
    @Getter
    public static class InsertValue {
        private ValueType type;
        private String value;

        public boolean isSelect() {
            return ValueType.SELECT == this.type;
        }

        public boolean isJdbc() {
            return ValueType.JDBC == this.type;
        }

        public int getJdbcParamIndex() {
            return Integer.parseInt(this.value.replace("?", ""));
        }

        private String getTrimString() {
            if (value != null && !value.isEmpty()) {
                if (value.startsWith("'") && value.endsWith("'")) {
                    return value.replace("'", "");
                }
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    return value.replace("\"", "");
                }
                return value;
            }
            return value;
        }

        public Object getValue(DbColumn dbColumn) {
            String value = this.getTrimString();
            if (value == null) {
                return null;
            }
            Class<?> jdbcType = dbColumn.getJavaType();
            if (jdbcType.equals(String.class)) {
                return value;
            }

            try {
                if (jdbcType.equals(Integer.class)) {
                    return Integer.parseInt(value);
                }
                if (jdbcType.equals(Long.class)) {
                    return Long.parseLong(value);
                }
                if (jdbcType.equals(Double.class)) {
                    return Double.parseDouble(value);
                }
                if (jdbcType.equals(Float.class)) {
                    return Float.parseFloat(value);
                }
                if (jdbcType.equals(BigDecimal.class)) {
                    return new BigDecimal(value);
                }
            } catch (Exception ignore) {
                return value;
            }
            if (jdbcType.equals(Boolean.class)) {
                return "true".equalsIgnoreCase(value);
            }
            return value;
        }
    }

    public static enum ValueType {
        STATIC,
        JDBC,
        SELECT
    }


}