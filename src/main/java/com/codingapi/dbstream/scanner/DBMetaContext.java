package com.codingapi.dbstream.scanner;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库元数据信息上下文对象
 */
public class DBMetaContext {

    @Getter
    private final static DBMetaContext instance = new DBMetaContext();

    private DBMetaContext() {
    }

    // key:jdbcKey,value:DBMetaData
    private final Map<String, DBMetaData> cache = new HashMap<>();

    /**
     * 获取元数据库的信息
     *
     * @param key 元数据信息key
     * @return 元数据信息
     */
    public DBMetaData getMetaData(String key) {
        return cache.get(key);
    }

    /**
     * 更新元数据库的信息
     *
     * @param metaData 元数据信息
     */
    public void update(DBMetaData metaData) {
        this.cache.put(metaData.getKeyJdbcKey(), metaData);
    }

    /**
     * 清空所有的元数据信息
     */
    public void clearAll() {
        for (DBMetaData metaData : this.cache.values()) {
            metaData.cleanSerializable();
        }
        cache.clear();
    }

    /**
     * 获取数据库的jdbc连接key信息
     * 可通过 {@link DBMetaContext#clear(String)}函数进行清空数据库的元数据信息
     *
     * @return [{"jdbc.url":"jdbc://...","jdbc.key::"1122..."}]
     */
    public List<String> loadDbKeys() {
        return new ArrayList<>(cache.keySet());
    }

    /**
     * 返回元数据信息列表
     *
     * @return 元数据信息列表
     */
    public List<DBMetaData> metaDataList() {
        return new ArrayList<>(cache.values());
    }


    /**
     * 通过jdbcKey {@link DBMetaData#KEY_JDBC_KEY} 清空指定数据库的元数据信息。
     * 可通过 {@link DBMetaContext#loadDbKeys()} 查看数据库的key信息。
     * 数据清空以后下次执行数据库访问时会自己重新加载元数据。
     *
     * @param jdbcKey 指定数据库信息
     */
    public void clear(String jdbcKey) {
        DBMetaData dbMetaData = cache.get(jdbcKey);
        if (dbMetaData != null) {
            dbMetaData.cleanSerializable();
        }
        cache.remove(jdbcKey);
    }

}
