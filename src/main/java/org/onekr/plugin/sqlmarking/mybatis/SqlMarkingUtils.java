package org.onekr.plugin.sqlmarking.mybatis;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * SQL染色工具类
 * 提供便捷的API用于设置染色信息和执行带染色的操作
 * 
 * @author Billy
 */
@Slf4j
public class SqlMarkingUtils {

    /**
     * 为当前线程设置用户ID
     */
    public static void setUserId(String userId) {
        SqlMarkingContext.setCurrentUserId(userId);
    }

    /**
     * 为当前线程添加自定义染色信息
     */
    public static void addCustomInfo(String key, Object value) {
        SqlMarkingContext.addCurrentCustomInfo(key, value);
    }

    /**
     * 为当前线程批量添加自定义染色信息
     */
    public static void addCustomInfo(Map<String, Object> customInfo) {
        if (customInfo != null && !customInfo.isEmpty()) {
            SqlMarkingContext context = SqlMarkingContext.getCurrentContext();
            if (context == null) {
                context = SqlMarkingContext.create();
            }
            context.addCustomInfo(customInfo);
        }
    }

    /**
     * 获取当前线程的用户ID
     */
    public static String getCurrentUserId() {
        return SqlMarkingContext.getCurrentUserId();
    }

    /**
     * 获取当前线程的自定义信息
     */
    public static Object getCurrentCustomInfo(String key) {
        SqlMarkingContext context = SqlMarkingContext.getCurrentContext();
        return context != null ? context.getCustomInfo(key) : null;
    }

    /**
     * 清除当前线程的染色上下文
     */
    public static void clearContext() {
        SqlMarkingContext.clearCurrentContext();
    }

    /**
     * 在指定用户上下文中执行操作
     */
    public static <T> T executeWithUserId(String userId, Callable<T> callable) throws Exception {
        SqlMarkingContext context = SqlMarkingContext.create(userId);
        return SqlMarkingContext.executeWithContext(context, () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 在指定用户上下文中执行操作（无返回值）
     */
    public static void executeWithUserId(String userId, Runnable runnable) {
        SqlMarkingContext context = SqlMarkingContext.create(userId);
        SqlMarkingContext.executeWithContext(context, runnable);
    }

    /**
     * 在指定自定义信息上下文中执行操作
     */
    public static <T> T executeWithCustomInfo(Map<String, Object> customInfo, Callable<T> callable) throws Exception {
        SqlMarkingContext context = SqlMarkingContext.create();
        context.addCustomInfo(customInfo);
        return SqlMarkingContext.executeWithContext(context, () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 在指定自定义信息上下文中执行操作（无返回值）
     */
    public static void executeWithCustomInfo(Map<String, Object> customInfo, Runnable runnable) {
        SqlMarkingContext context = SqlMarkingContext.create();
        context.addCustomInfo(customInfo);
        SqlMarkingContext.executeWithContext(context, runnable);
    }

    /**
     * 在完整上下文中执行操作
     */
    public static <T> T executeWithContext(String userId, Map<String, Object> customInfo, Callable<T> callable) throws Exception {
        SqlMarkingContext context = SqlMarkingContext.create(userId);
        if (customInfo != null) {
            context.addCustomInfo(customInfo);
        }
        return SqlMarkingContext.executeWithContext(context, () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 在完整上下文中执行操作（无返回值）
     */
    public static void executeWithContext(String userId, Map<String, Object> customInfo, Runnable runnable) {
        SqlMarkingContext context = SqlMarkingContext.create(userId);
        if (customInfo != null) {
            context.addCustomInfo(customInfo);
        }
        SqlMarkingContext.executeWithContext(context, runnable);
    }

    /**
     * 添加客户端线程ID到染色信息
     */
    public static void addClientThreadId(String clientThreadId) {
        addCustomInfo("clientThreadId", clientThreadId);
    }

    /**
     * 添加请求ID到染色信息
     */
    public static void addRequestId(String requestId) {
        addCustomInfo("requestId", requestId);
    }

    /**
     * 添加业务标识到染色信息
     */
    public static void addBusinessTag(String businessTag) {
        addCustomInfo("businessTag", businessTag);
    }

    /**
     * 添加模块标识到染色信息
     */
    public static void addModuleTag(String moduleTag) {
        addCustomInfo("moduleTag", moduleTag);
    }

    /**
     * 创建染色上下文构建器
     */
    public static ContextBuilder contextBuilder() {
        return new ContextBuilder();
    }

    /**
     * 染色上下文构建器
     */
    public static class ContextBuilder {
        private final SqlMarkingContext context;

        public ContextBuilder() {
            this.context = new SqlMarkingContext();
        }

        public ContextBuilder userId(String userId) {
            context.setUserId(userId);
            return this;
        }

        public ContextBuilder customInfo(String key, Object value) {
            context.addCustomInfo(key, value);
            return this;
        }

        public ContextBuilder customInfo(Map<String, Object> customInfo) {
            context.addCustomInfo(customInfo);
            return this;
        }

        public ContextBuilder clientThreadId(String clientThreadId) {
            return customInfo("clientThreadId", clientThreadId);
        }

        public ContextBuilder requestId(String requestId) {
            return customInfo("requestId", requestId);
        }

        public ContextBuilder businessTag(String businessTag) {
            return customInfo("businessTag", businessTag);
        }

        public ContextBuilder moduleTag(String moduleTag) {
            return customInfo("moduleTag", moduleTag);
        }

        public <T> T execute(Callable<T> callable) throws Exception {
            return SqlMarkingContext.executeWithContext(context, () -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void execute(Runnable runnable) {
            SqlMarkingContext.executeWithContext(context, runnable);
        }

        public SqlMarkingContext build() {
            return context;
        }

        public void apply() {
            SqlMarkingContext.setCurrentContext(context);
        }
    }
}