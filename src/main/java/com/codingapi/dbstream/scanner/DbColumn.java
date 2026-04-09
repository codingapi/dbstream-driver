package com.codingapi.dbstream.scanner;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 表字段元数据信息
 */
@Setter
@Getter
public class DbColumn implements Serializable {

    /**
     * 字段名称
     */
    private String name;
    /**
     * 数据库字段类型
     */
    private String type;

    /**
     * java数据对应类型
     */
    private Class<?> javaType;
    /**
     * 是否可为空
     */
    private boolean nullable;
    /**
     * 字段备注
     */
    private String comment;
    /**
     * 字段长度
     */
    private int size;
    /**
     * 是否为主键
     */
    private boolean primaryKey;
}
