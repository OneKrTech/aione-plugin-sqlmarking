package org.aione.plugin.sqlmarking;

import lombok.extern.slf4j.Slf4j;

/**
 * SQL染色处理器
 * 负责将染色信息注入到SQL语句中
 * 
 * @author Billy
 */
@Slf4j
public class SqlMarkingProcessor {

    /**
     * 默认配置
     */
    private SqlMarkingConfig config;

    public SqlMarkingProcessor() {
        this.config = new SqlMarkingConfig();
    }

    public SqlMarkingProcessor(SqlMarkingConfig config) {
        this.config = config;
    }

    /**
     * 对SQL进行标记处理
     * 
     * @param originalSql 原始SQL语句
     * @param markingInfo 标记信息
     * @return 标记后的SQL语句
     */
    public String markSql(String originalSql, SqlMarkingInfo markingInfo) {
        if (originalSql == null || originalSql.trim().isEmpty()) {
            return originalSql;
        }

        if (markingInfo == null) {
            log.warn("标记信息为空，返回原始SQL");
            return originalSql;
        }

        try {
            // 构建标记注释
            String markingComment = buildMarkingComment(markingInfo);
            
            // 将标记注释插入到SQL中
            return insertMarkingComment(originalSql, markingComment);
            
        } catch (Exception e) {
            log.error("SQL标记处理异常，返回原始SQL: {}", e.getMessage(), e);
            return originalSql;
        }
    }

    /**
     * 构建标记注释
     */
    private String buildMarkingComment(SqlMarkingInfo markingInfo) {
        StringBuilder comment = new StringBuilder();
        comment.append(config.getMarkPrefix());

        if (config.isIncludeFullInfo()) {
            // 包含完整信息
            comment.append(markingInfo.getFullInfoString());
        } else {
            // 只包含简化信息
            comment.append(markingInfo.getSimpleInfoString());
        }

        comment.append(config.getMarkSuffix());
        return comment.toString();
    }

    /**
     * 将标记注释插入到SQL中
     * 策略：在SQL语句的开头插入注释，不影响SQL的执行计划
     */
    private String insertMarkingComment(String originalSql, String markingComment) {
        String trimmedSql = originalSql.trim();
        
        // 检查SQL是否已经以注释开头
        if (trimmedSql.startsWith("/*")) {
            // 如果已有注释，在第一个注释后插入标记注释
            int commentEndIndex = trimmedSql.indexOf("*/");
            if (commentEndIndex > 0) {
                String beforeComment = trimmedSql.substring(0, commentEndIndex + 2);
                String afterComment = trimmedSql.substring(commentEndIndex + 2);
                return beforeComment + " " + markingComment + afterComment;
            }
        }
        
        // 在SQL开头插入标记注释
        return markingComment + " " + trimmedSql;
    }

    /**
     * 检查SQL是否已经被标记
     */
    public boolean isAlreadyMarked(String sql) {
        if (sql == null) {
            return false;
        }
        return sql.contains(config.getMarkPrefix()) && sql.contains(config.getMarkSuffix());
    }

    /**
     * 从标记的SQL中提取原始SQL
     */
    public String extractOriginalSql(String markedSql) {
        if (markedSql == null || !isAlreadyMarked(markedSql)) {
            return markedSql;
        }

        try {
            int prefixIndex = markedSql.indexOf(config.getMarkPrefix());
            int suffixIndex = markedSql.indexOf(config.getMarkSuffix(), prefixIndex);
            
            if (prefixIndex >= 0 && suffixIndex > prefixIndex) {
                // 移除标记注释
                String beforeComment = markedSql.substring(0, prefixIndex);
                String afterComment = markedSql.substring(suffixIndex + config.getMarkSuffix().length());
                return (beforeComment + afterComment).trim();
            }
            
        } catch (Exception e) {
            log.warn("提取原始SQL失败: {}", e.getMessage());
        }
        
        return markedSql;
    }

    /**
     * 从标记的SQL中提取标记信息
     */
    public String extractMarkingInfo(String markedSql) {
        if (markedSql == null || !isAlreadyMarked(markedSql)) {
            return null;
        }

        try {
            int prefixIndex = markedSql.indexOf(config.getMarkPrefix());
            int suffixIndex = markedSql.indexOf(config.getMarkSuffix(), prefixIndex);
            
            if (prefixIndex >= 0 && suffixIndex > prefixIndex) {
                int infoStart = prefixIndex + config.getMarkPrefix().length();
                return markedSql.substring(infoStart, suffixIndex).trim();
            }
            
        } catch (Exception e) {
            log.warn("提取标记信息失败: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * 验证标记后的SQL语法正确性
     * 主要检查注释格式是否正确
     */
    public boolean validateMarkedSql(String markedSql) {
        if (markedSql == null || markedSql.trim().isEmpty()) {
            return false;
        }

        // 检查标记注释的格式
        if (isAlreadyMarked(markedSql)) {
            String markingInfo = extractMarkingInfo(markedSql);
            return markingInfo != null && !markingInfo.isEmpty();
        }

        return true;
    }

    /**
     * 设置配置
     */
    public void setConfig(SqlMarkingConfig config) {
        this.config = config;
    }

    /**
     * 获取配置
     */
    public SqlMarkingConfig getConfig() {
        return config;
    }
}