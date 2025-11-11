package com.codingapi.dbstream.utils;

import com.codingapi.dbstream.scanner.DbTable;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DBTableSerializableHelper {

    private final File path;

    public DBTableSerializableHelper(String jdbcKey) {
        this.path = new File("./.dbstream/" + jdbcKey + "/");
        if (!this.path.exists()) {
            boolean parentResult = path.getParentFile().mkdir();
            boolean currentResult = path.mkdir();
            boolean result = parentResult && currentResult;
            System.out.println("Serializable Table " + result + " File Path:" + this.path);
        }
    }

    public void clean() {
        if (path.exists() && path.isDirectory()) {
            deleteRecursively(path);
        }
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
            System.err.println("⚠️ 无法删除文件: " + file.getAbsolutePath());
        }
    }

    public boolean hasSerialize(String tableName) {
        File file = new File(this.path.getPath() + "/" + tableName);
        return file.exists();
    }

    public void serialize(DbTable dbTable) {
        String fileName = dbTable.getName();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(this.path + "/" + fileName)))) {
            oos.writeObject(dbTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public DbTable deserialize(String tableName) {
        File file = new File(this.path.getPath() + "/" + tableName);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
            return (DbTable) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
