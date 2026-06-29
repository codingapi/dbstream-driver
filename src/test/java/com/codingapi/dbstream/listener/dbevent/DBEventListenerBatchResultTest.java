package com.codingapi.dbstream.listener.dbevent;

import com.codingapi.dbstream.listener.SQLRunningState;
import com.codingapi.dbstream.parser.DBEventParser;
import com.codingapi.dbstream.parser.SQLParser;
import com.codingapi.dbstream.scanner.DbTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DBEventListener#batchResultToArrays(Object, int)} 规范化逻辑测试。
 * 重点回归：JDBC 驱动返回的结果数组长度与批量行数不一致时，不得越界且事件不得丢失。
 */
class DBEventListenerBatchResultTest {

    private DBEventListener newListener() {
        return new DBEventListener() {
            @Override
            public int order() {
                return 0;
            }

            @Override
            public boolean support(String sql) {
                return false;
            }

            @Override
            public SQLParser createSQLParser(String sql) {
                return null;
            }

            @Override
            public DBEventParser createDbEventParser(SQLRunningState runningState, SQLParser sqlParser, DbTable dbTable) {
                return null;
            }
        };
    }

    @Test
    void intArrayLengthEqualsSize() {
        DBEventListener listener = newListener();
        List<Object> arrays = listener.batchResultToArrays(new int[]{1, 1, 1}, 3);
        assertEquals(3, arrays.size());
        assertEquals(1, arrays.get(0));
        assertEquals(1, arrays.get(2));
    }

    /**
     * 回归 rewriteBatchedStatements 等场景：驱动返回数组长度小于批量行数，
     * 修复前 List 长度=驱动数组长度，after() 中 arrays.get(i) 会 IndexOutOfBounds。
     */
    @Test
    void intArrayLengthLessThanSize_notOutOfBounds() {
        DBEventListener listener = newListener();
        // 批量行数 636，驱动合并后仅返回 635 个结果（复现线上越界场景）
        List<Object> arrays = listener.batchResultToArrays(new int[]{1, 1, 1}, 636);
        assertEquals(636, arrays.size());
        // 末位由「成功」哨兵补齐，必须可安全访问
        assertEquals(1, arrays.get(635));
    }

    @Test
    void intArrayLengthGreaterThanSize_truncated() {
        DBEventListener listener = newListener();
        List<Object> arrays = listener.batchResultToArrays(new int[]{1, 2, 3, 4}, 2);
        assertEquals(2, arrays.size());
        assertEquals(1, arrays.get(0));
        assertEquals(2, arrays.get(1));
    }

    @Test
    void longArrayLengthLessThanSize_notOutOfBounds() {
        DBEventListener listener = newListener();
        List<Object> arrays = listener.batchResultToArrays(new long[]{1L, 2L}, 4);
        assertEquals(4, arrays.size());
        assertEquals(1L, arrays.get(0));
        assertEquals(1L, arrays.get(3));
    }

    @Test
    void nonArrayResult_fallbackAllZero() {
        DBEventListener listener = newListener();
        List<Object> arrays = listener.batchResultToArrays(5, 3);
        assertEquals(3, arrays.size());
        assertEquals(0, arrays.get(0));
        assertEquals(0, arrays.get(2));
    }
}
