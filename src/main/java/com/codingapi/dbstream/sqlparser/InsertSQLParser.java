package com.codingapi.dbstream.sqlparser;

import com.codingapi.dbstream.utils.SQLUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertSQLParser {

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


    public List<InsertValue> getValues(){
        List<InsertValue> insertValues = new ArrayList<>();
        List<String> values = SQLUtils.parseValues(this.getValuesSQL());
        for(String value:values){
            InsertValue insertValue = new InsertValue();
            if(value.trim().equals("?")){
                insertValue.setType(ValueType.JDBC);
            }else if(SQLUtils.isSQLKeyword(value.trim()) || value.trim().startsWith("(")){
                insertValue.setType(ValueType.SELECT);
            }else {
                insertValue.setType(ValueType.STATIC);
            }
            insertValue.setValue(value);
            insertValues.add(insertValue);
        }
        return insertValues;
    }

    @Setter
    @Getter
    public static class InsertValue{
        private ValueType type;
        private String value;

        public boolean isSelect(){
            return ValueType.SELECT == this.type;
        }

        public boolean isJdbc(){
            return ValueType.JDBC == this.type;
        }
    }

    public static enum ValueType{
        STATIC,
        JDBC,
        SELECT
    }


}