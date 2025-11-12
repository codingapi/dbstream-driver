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
     * 字段类型
     */
    private String type;
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
