package com.codingapi.dbstream.listener;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * SQL执行参数信息
 */
public class SQLRunningParam {


    /**
     * SQL参数,integer index模式
     */
    @Getter
    private final Map<Integer, Object> indexParams;
    /**
     * SQL参数,string key 模型
     */
    @Getter
    private final Map<String, Object> mapParams;

    /**
     * 执行的sql
     */
    @Getter
    @Setter
    private String sql;

    public SQLRunningParam() {
        this.indexParams = new HashMap<>();
        this.mapParams = new HashMap<>();
    }


    /**
     * 更新sql参数
     *
     * @param key   参数key
     * @param value 参数值
     */
    public void setParam(String key, Object value) {
        mapParams.put(key, value);
    }

    /**
     * 更新sql参数
     *
     * @param index 参数索引
     * @param value 参数值
     */
    public void setParam(int index, Object value) {
        indexParams.put(index, value);
    }

    /**
     * 清理参数
     */
    public void cleanParams() {
        this.indexParams.clear();
        this.mapParams.clear();
    }

    /**
     * 获取参数列表
     *
     * @return List
     */
    public List<Object> getListParams() {
        List<Object> list = new ArrayList<>();
        if (indexParams.isEmpty()) {
            return list;
        }
        List<Integer> keys = new ArrayList<>(indexParams.keySet());
        Collections.sort(keys);
        for (Integer key : keys) {
            list.add(indexParams.get(key));
        }
        return list;
    }
}
