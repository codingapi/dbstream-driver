package com.codingapi.dbstream.serializable;

import com.codingapi.dbstream.scanner.DbTable;
import com.codingapi.dbstream.utils.FileReaderUtils;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 元数据数据序列化助手
 */
public class DBTableSerializableHelper {

    private static final Logger LOGGER = Logger.getLogger(DBTableSerializableHelper.class.getName());

    // 数据库序列化文件存储位置
    private final File path;

    /**
     * 构造函数
     *
     * @param jdbcKey 数据库的唯一标识
     */
    public DBTableSerializableHelper(String jdbcKey) {
        this.path = new File("./.dbstream/" + jdbcKey + "/");
        if (!this.path.exists()) {
            boolean result = path.mkdirs();
            LOGGER.log(Level.INFO, "Serializable Table directory created: {0}, File Path: {1}",
                    new Object[]{result, this.path.getAbsolutePath()});
        }
    }

    /**
     * 删除全部序列化文件
     */
    public void remove() {
        if (path.exists() && path.isDirectory()) {
            deleteRecursively(path);
        }
    }

    /**
     * 通过表名称读取手动配置主键信息
     */
    public List<String> loadTablePrimaryKeysByKeyFile(String tableName) {
        File file = new File(this.path + "/" + tableName + ".key");
        List<String> data = FileReaderUtils.read(file);
        if (data != null && !data.isEmpty()) {
            List<String> columns = new ArrayList<>();
            for (String line : data) {
                columns.addAll(Arrays.asList(line.split(",")));
            }
            return columns;
        }
        return null;
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursively(f);
                }
            }
        }
        boolean deleted = file.delete();
        if (!deleted) {
            LOGGER.log(Level.WARNING, "Failed to delete file: {0}", file.getAbsolutePath());
        }
    }

    /**
     * 是否存在序列化的表数据
     */
    public boolean hasSerialize(String tableName) {
        File file = new File(this.path.getPath() + "/" + tableName);
        return file.exists();
    }

    /**
     * 序列化表数据
     */
    public void serialize(DbTable dbTable) {
        String fileName = dbTable.getName();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(this.path + "/" + fileName)))) {
            oos.writeObject(dbTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 反序列化数据
     */
    public DbTable deserialize(String tableName) {
        File file = new File(this.path.getPath() + "/" + tableName);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
            return (DbTable) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
