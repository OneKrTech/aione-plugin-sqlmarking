package org.aione.plugin.sqlmarking.example.plugin.sqlmarking;

import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL染色信息类
 * 包含所有染色相关的元数据信息
 * 
 * @author Billy
 */
@Data
public class SqlMarkingInfo {

    /**
     * MyBatis StatementId，用于标识具体的SQL语句
     */
    private String statementId;

    /**
     * 分布式追踪标识符，用于跨服务追踪
     */
    private String pFinderId;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * SQL命令类型
     */
    private SqlCommandType sqlCommandType;

    /**
     * 执行序号
     */
    private Long executionId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 执行线程ID
     */
    private Long threadId;

    /**
     * 用户ID（可选）
     */
    private String userId;

    /**
     * 自定义染色信息
     */
    private ConcurrentHashMap<String, Object> customInfo;

    /**
     * 获取简化的StatementId（去除包名前缀）
     */
    public String getSimpleStatementId() {
        if (statementId == null) {
            return null;
        }
        int lastDotIndex = statementId.lastIndexOf('.');
        return lastDotIndex > 0 ? statementId.substring(lastDotIndex + 1) : statementId;
    }

    /**
     * 获取SQL命令类型的简短名称
     */
    public String getSqlCommandTypeName() {
        return sqlCommandType != null ? sqlCommandType.name() : "UNKNOWN";
    }

    /**
     * 检查是否包含自定义信息
     */
    public boolean hasCustomInfo() {
        return customInfo != null && !customInfo.isEmpty();
    }

    /**
     * 获取自定义信息的字符串表示
     */
    public String getCustomInfoString() {
        if (!hasCustomInfo()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        customInfo.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(key).append("=").append(value);
        });
        return sb.toString();
    }

    /**
     * 添加自定义信息
     */
    public void addCustomInfo(String key, Object value) {
        if (customInfo == null) {
            customInfo = new ConcurrentHashMap<>();
        }
        customInfo.put(key, value);
    }

    /**
     * 获取染色信息的完整字符串表示
     */
    public String getFullInfoString() {
        StringBuilder sb = new StringBuilder();
        
        // StatementId
        if (statementId != null) {
            sb.append("stmt=").append(getSimpleStatementId());
        }

        // PFinderId
        if (pFinderId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("pf=").append(pFinderId);
        }

        // TraceId
        if (traceId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("trace=").append(traceId);
        }
        
        // ExecutionId
        if (executionId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("exec=").append(executionId);
        }
        
        // ThreadId
        if (threadId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("thread=").append(threadId);
        }
        
        // UserId
        if (userId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("user=").append(userId);
        }
        
        // Timestamp
        if (timestamp != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("ts=").append(timestamp);
        }
        
        // Custom Info
        if (hasCustomInfo()) {
            if (sb.length() > 0) sb.append("|");
            sb.append("custom=").append(getCustomInfoString());
        }
        
        return sb.toString();
    }

    /**
     * 获取简化的染色信息字符串
     */
    public String getSimpleInfoString() {
        StringBuilder sb = new StringBuilder();

        // PFinderId (最重要的追踪标识)
        if (pFinderId != null) {
            sb.append("pf=").append(pFinderId);
        }

        // ExecutionId
        if (executionId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("exec=").append(executionId);
        }
        
        // ThreadId
        if (threadId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append("t=").append(threadId);
        }
        
        return sb.toString();
    }
}