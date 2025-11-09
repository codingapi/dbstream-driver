package com.codingapi.dbstream.scanner;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DbColumn {

    private String name;
    private String type;
    private boolean nullable;
    private String comment;
    private int size;
    private boolean primaryKey;
}
