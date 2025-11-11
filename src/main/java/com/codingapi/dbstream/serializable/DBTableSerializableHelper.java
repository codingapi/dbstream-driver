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

public class DBTableSerializableHelper {

    private static final Logger LOGGER = Logger.getLogger(DBTableSerializableHelper.class.getName());
    private final File path;

    public DBTableSerializableHelper(String jdbcKey) {
        this.path = new File("./.dbstream/" + jdbcKey + "/");
        if (!this.path.exists()) {
            boolean result = path.mkdirs();
            LOGGER.log(Level.INFO, "Serializable Table directory created: {0}, File Path: {1}",
                    new Object[]{result, this.path.getAbsolutePath()});
        }
    }

    public void clean() {
        if (path.exists() && path.isDirectory()) {
            deleteRecursively(path);
        }
    }

    public List<String> loadPrimaryKeyByLocalFile(String tableName) {
        File file = new File(this.path + "/" + tableName + ".key");
        List<String> data =  FileReaderUtils.read(file);
        if(data!=null && !data.isEmpty()) {
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
