package com.codingapi.dbstream.event;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * DBEvent 事件跳过控制上下文（线程级）。
 * <p>
 * 在真实执行 DML 之前，通过 {@code DBStreamContext.skipNextEvent(...)} 打上一次性跳过标记：
 * <ul>
 *     <li>语句级规则（仅表名+事件类型，无数据匹配条件）：在 SQL 执行前置阶段命中即消费，
 *     整条语句不产生事件，同时省去前镜像查询开销。</li>
 *     <li>行级规则（带数据列值匹配 Map 或自定义断言 Predicate）：在事件生成后逐条过滤，
 *     实际抑制了至少一个事件才消费；同一语句（含批量）内所有命中的事件都会被跳过。</li>
 * </ul>
 * 注意：打标记的线程必须与执行 SQL 的线程一致（ThreadLocal 语义）；
 * 一直未命中的规则会保留到 {@link #clear()} 被调用或数据库连接关闭时，
 * 建议标记紧贴 DML 执行，或在 finally 中调用 clear 兜底。
 */
public class DBEventSkipContext {

    @Getter
    private final static DBEventSkipContext instance = new DBEventSkipContext();

    /**
     * 当前线程的跳过规则列表（一次性消费，消费后自动移除）
     */
    private final ThreadLocal<List<SkipRule>> rules = new ThreadLocal<>();

    private DBEventSkipContext() {
    }

    /**
     * 跳过下一条匹配语句的全部事件（不限类型）
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     */
    public void skipNextEvent(String tableName) {
        this.addRule(new SkipRule(tableName, null, null, null));
    }

    /**
     * 跳过下一条匹配语句的全部事件（限定事件类型）
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     * @param type      事件类型，null 表示所有类型
     */
    public void skipNextEvent(String tableName, EventType type) {
        this.addRule(new SkipRule(tableName, type, null, null));
    }

    /**
     * 按数据列值匹配跳过事件（行级），实际抑制了事件才消费规则。
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     * @param type      事件类型，null 表示所有类型
     * @param dataMatch 数据列值匹配条件（列名忽略大小写，值兼容数值类型差异），null 或空表示无条件
     */
    public void skipNextEvent(String tableName, EventType type, Map<String, Object> dataMatch) {
        this.addRule(new SkipRule(tableName, type,
                (dataMatch == null || dataMatch.isEmpty()) ? null : dataMatch, null));
    }

    /**
     * 按自定义断言跳过事件（行级），实际抑制了事件才消费规则。
     *
     * @param tableName 表名（忽略大小写），null 表示不限表
     * @param type      事件类型，null 表示所有类型
     * @param predicate 事件断言，返回 true 表示跳过该事件
     */
    public void skipNextEvent(String tableName, EventType type, Predicate<DBEvent> predicate) {
        this.addRule(new SkipRule(tableName, type, null, predicate));
    }

    /**
     * 清空当前线程的所有跳过标记
     */
    public void clear() {
        rules.remove();
    }

    private void addRule(SkipRule rule) {
        List<SkipRule> current = rules.get();
        if (current == null) {
            current = new ArrayList<>();
            rules.set(current);
        }
        current.add(rule);
    }

    /**
     * 语句级跳过判定（SQL 执行前置阶段调用）。
     * 匹配无行级条件的规则，命中即消费该规则并返回 true（调用方应短路，不产生事件）。
     *
     * @param tableName 元数据规范表名
     * @param type      当前语句的事件类型
     * @return 是否跳过该语句的事件
     */
    public boolean skipStatement(String tableName, EventType type) {
        List<SkipRule> current = rules.get();
        if (current == null || current.isEmpty()) {
            return false;
        }
        Iterator<SkipRule> iterator = current.iterator();
        while (iterator.hasNext()) {
            SkipRule rule = iterator.next();
            if (!rule.isRowLevel() && rule.matchTableAndType(tableName, type)) {
                // 一次性消费
                iterator.remove();
                if (current.isEmpty()) {
                    rules.remove();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 行级跳过过滤（事件生成后、进入事务事件池前调用）。
     * 命中行级规则的事件被剔除；被命中的规则在本批事件处理完后统一消费移除，
     * 因此同一语句（含批量、多行VALUES）内所有命中的事件都会被跳过。
     *
     * @param events 解析生成的事件列表
     * @return 过滤后的事件列表
     */
    public List<DBEvent> filterEvents(List<DBEvent> events) {
        if (events == null || events.isEmpty()) {
            return events;
        }
        List<SkipRule> current = rules.get();
        if (current == null || current.isEmpty()) {
            return events;
        }
        List<DBEvent> result = new ArrayList<>(events.size());
        Set<SkipRule> consumed = new HashSet<>();
        for (DBEvent event : events) {
            SkipRule hit = null;
            for (SkipRule rule : current) {
                if (rule.isRowLevel() && rule.matchEvent(event)) {
                    hit = rule;
                    break;
                }
            }
            if (hit != null) {
                consumed.add(hit);
            } else {
                result.add(event);
            }
        }
        if (!consumed.isEmpty()) {
            current.removeAll(consumed);
            if (current.isEmpty()) {
                rules.remove();
            }
        }
        return result;
    }

    /**
     * 跳过规则：tableName/type 为 null 表示不限；
     * dataMatch 与 predicate 均为 null 时为语句级规则，否则为行级规则。
     */
    private static class SkipRule {

        private final String tableName;
        private final EventType type;
        private final Map<String, Object> dataMatch;
        private final Predicate<DBEvent> predicate;

        SkipRule(String tableName, EventType type, Map<String, Object> dataMatch, Predicate<DBEvent> predicate) {
            this.tableName = tableName;
            this.type = type;
            this.dataMatch = dataMatch;
            this.predicate = predicate;
        }

        /**
         * 是否行级规则（带数据匹配条件）
         */
        boolean isRowLevel() {
            return dataMatch != null || predicate != null;
        }

        /**
         * 表名与事件类型匹配（表名忽略大小写，类型为 null 表示所有类型）
         */
        boolean matchTableAndType(String tableName, EventType type) {
            if (this.tableName != null && !this.tableName.equalsIgnoreCase(tableName)) {
                return false;
            }
            return this.type == null || this.type == type;
        }

        /**
         * 事件级匹配：表名+类型+自定义断言+数据列值匹配
         */
        boolean matchEvent(DBEvent event) {
            if (!matchTableAndType(event.getTableName(), event.getType())) {
                return false;
            }
            if (predicate != null && !predicate.test(event)) {
                return false;
            }
            if (dataMatch != null) {
                for (Map.Entry<String, Object> entry : dataMatch.entrySet()) {
                    Object actual = getValueIgnoreCase(event.getData(), entry.getKey());
                    if (!valueEquals(actual, entry.getValue())) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * 忽略列名大小写从事件数据中取值（事件 data 的 key 为元数据规范大小写）
         */
        private static Object getValueIgnoreCase(Map<String, Object> data, String key) {
            if (data == null || key == null) {
                return null;
            }
            Object value = data.get(key);
            if (value != null) {
                return value;
            }
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (key.equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return null;
        }

        /**
         * 值比较：优先 Objects.equals，失败后回退字符串比较，
         * 兼容 JDBC 数值类型差异（如用户传 Integer 1001 而数据库返回 Long 1001）。
         */
        private static boolean valueEquals(Object actual, Object expected) {
            if (Objects.equals(actual, expected)) {
                return true;
            }
            if (actual == null || expected == null) {
                return false;
            }
            return String.valueOf(actual).equals(String.valueOf(expected));
        }
    }

}
