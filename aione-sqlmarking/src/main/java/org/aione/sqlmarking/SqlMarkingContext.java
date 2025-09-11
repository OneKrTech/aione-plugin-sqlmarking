package org.aione.sqlmarking;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL染色上下文管理器
 * 用于在当前线程中传递自定义染色信息
 * 
 * @author Billy
 */
@Slf4j
@Data
public class SqlMarkingContext {

    /**
     * 线程本地存储
     */
    private static final InheritableThreadLocal<SqlMarkingContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    /**
     * 线程ID
     */
    private Long threadId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 分布式追踪标识符，用于跨服务追踪
     */
    private String pFinderId;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 自定义染色信息
     */
    private ConcurrentHashMap<String, Object> customInfo;

    /**
     * 上下文创建时间
     */
    private Long createTime;

    public SqlMarkingContext() {
        this.threadId = Thread.currentThread().getId();
        this.createTime = System.currentTimeMillis();
        this.customInfo = new ConcurrentHashMap<>();
    }

    /**
     * 获取当前线程的染色上下文
     * 
     * @return 当前线程的SqlMarkingContext实例，如果未设置则返回null
     */
    public static SqlMarkingContext getCurrentContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 设置当前线程的染色上下文
     * 
     * @param context 要设置的SqlMarkingContext实例
     */
    public static void setCurrentContext(SqlMarkingContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 清除当前线程的染色上下文
     */
    public static void clearCurrentContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 创建新的染色上下文
     * 
     * @return 新创建的SqlMarkingContext实例
     */
    public static SqlMarkingContext create() {
        SqlMarkingContext context = new SqlMarkingContext();
        setCurrentContext(context);
        return context;
    }

    /**
     * 创建带用户ID的染色上下文
     * 
     * @param userId 用户ID
     * @return 新创建的SqlMarkingContext实例
     */
    public static SqlMarkingContext create(String userId) {
        SqlMarkingContext context = create();
        context.setUserId(userId);
        return context;
    }

    /**
     * 添加自定义信息
     * 
     * @param key 自定义信息的键
     * @param value 自定义信息的值
     * @return 当前SqlMarkingContext实例，支持链式调用
     */
    public SqlMarkingContext addCustomInfo(String key, Object value) {
        if (customInfo == null) {
            customInfo = new ConcurrentHashMap<>();
        }
        customInfo.put(key, value);
        return this;
    }

    /**
     * 批量添加自定义信息
     * 
     * @param info 包含自定义信息的Map
     * @return 当前SqlMarkingContext实例，支持链式调用
     */
    public SqlMarkingContext addCustomInfo(Map<String, Object> info) {
        if (info != null && !info.isEmpty()) {
            if (customInfo == null) {
                customInfo = new ConcurrentHashMap<>();
            }
            customInfo.putAll(info);
        }
        return this;
    }

    /**
     * 获取自定义信息
     * 
     * @param key 自定义信息的键
     * @return 对应的自定义信息值，如果不存在则返回null
     */
    public Object getCustomInfo(String key) {
        return customInfo != null ? customInfo.get(key) : null;
    }

    /**
     * 检查是否包含自定义信息
     * 
     * @return 如果包含自定义信息返回true，否则返回false
     */
    public boolean hasCustomInfo() {
        return customInfo != null && !customInfo.isEmpty();
    }

    /**
     * 获取自定义信息的大小
     * 
     * @return 自定义信息的数量
     */
    public int getCustomInfoSize() {
        return customInfo != null ? customInfo.size() : 0;
    }

    /**
     * 移除自定义信息
     * 
     * @param key 要移除的自定义信息的键
     * @return 被移除的自定义信息值，如果不存在则返回null
     */
    public Object removeCustomInfo(String key) {
        return customInfo != null ? customInfo.remove(key) : null;
    }

    /**
     * 清空自定义信息
     */
    public void clearCustomInfo() {
        if (customInfo != null) {
            customInfo.clear();
        }
    }

    /**
     * 获取上下文的字符串表示
     */
    @Override
    public String toString() {
        return String.format("SqlMarkingContext[threadId=%d, userId=%s, customInfoSize=%d, createTime=%d]",
            threadId, userId, getCustomInfoSize(), createTime);
    }

    /**
     * 静态工具方法：在指定上下文中执行操作
     * 
     * @param <T> 返回值类型
     * @param context 要使用的SqlMarkingContext实例
     * @param supplier 要执行的操作
     * @return 操作的返回值
     */
    public static <T> T executeWithContext(SqlMarkingContext context, java.util.function.Supplier<T> supplier) {
        SqlMarkingContext originalContext = getCurrentContext();
        try {
            setCurrentContext(context);
            return supplier.get();
        } finally {
            if (originalContext != null) {
                setCurrentContext(originalContext);
            } else {
                clearCurrentContext();
            }
        }
    }

    /**
     * 静态工具方法：在指定上下文中执行操作（无返回值）
     * 
     * @param context 要使用的SqlMarkingContext实例
     * @param runnable 要执行的操作
     */
    public static void executeWithContext(SqlMarkingContext context, Runnable runnable) {
        SqlMarkingContext originalContext = getCurrentContext();
        try {
            setCurrentContext(context);
            runnable.run();
        } finally {
            if (originalContext != null) {
                setCurrentContext(originalContext);
            } else {
                clearCurrentContext();
            }
        }
    }

    /**
     * 静态工具方法：为当前线程添加自定义信息
     * 
     * @param key 自定义信息的键
     * @param value 自定义信息的值
     */
    public static void addCurrentCustomInfo(String key, Object value) {
        SqlMarkingContext context = getCurrentContext();
        if (context == null) {
            context = create();
        }
        context.addCustomInfo(key, value);
    }

    /**
     * 静态工具方法：为当前线程设置用户ID
     * 
     * @param userId 要设置的用户ID
     */
    public static void setCurrentUserId(String userId) {
        SqlMarkingContext context = getCurrentContext();
        if (context == null) {
            context = create();
        }
        context.setUserId(userId);
    }

    /**
     * 静态工具方法：获取当前线程的用户ID
     * 
     * @return 当前线程的用户ID，如果未设置则返回null
     */
    public static String getCurrentUserId() {
        SqlMarkingContext context = getCurrentContext();
        return context != null ? context.getUserId() : null;
    }
}