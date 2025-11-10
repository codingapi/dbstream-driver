package com.codingapi.dbstream.scanner;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class DbColumn implements Serializable {

    private String name;
    private String type;
    private boolean nullable;
    private String comment;
    private int size;
    private boolean primaryKey;
}
