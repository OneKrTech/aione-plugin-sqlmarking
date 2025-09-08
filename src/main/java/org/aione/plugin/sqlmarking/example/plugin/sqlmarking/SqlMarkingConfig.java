package org.aione.plugin.sqlmarking.example.plugin.sqlmarking;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * SQL标记配置类
 * 
 * @author Billy
 */
@Data
@ConfigurationProperties(prefix = "mybatis.sql-marking")
public class SqlMarkingConfig {


    /**
     * 是否启用SQL染色功能
     */
    private boolean enabled = true;

    /**
     * 是否对SELECT语句进行染色
     */
    private boolean markSelect = true;

    /**
     * 是否对INSERT语句进行染色
     */
    private boolean markInsert = true;

    /**
     * 是否对UPDATE语句进行染色
     */
    private boolean markUpdate = true;

    /**
     * 是否对DELETE语句进行染色
     */
    private boolean markDelete = true;

    /**
     * 是否启用调试模式
     */
    private boolean debugEnabled = false;

    /**
     * 是否启用详细日志记录
     */
    private boolean verboseLogging = false;

    /**
     * 标记前缀
     */
    private String markPrefix = "/* MARKED ";

    /**
     * 标记后缀
     */
    private String markSuffix = " END_MARKED */";

    /**
     * 是否在SQL注释中包含完整的标记信息
     */
    private boolean includeFullInfo = true;

    /**
     * 是否包含时间戳信息
     */
    private boolean includeTimestamp = true;

    /**
     * 是否包含线程信息
     */
    private boolean includeThreadInfo = true;

    /**
     * 是否包含自定义信息
     */
    private boolean includeCustomInfo = true;

    /**
     * 最大自定义信息长度
     */
    private int maxCustomInfoLength = 200;

    /**
     * 标记信息分隔符
     */
    private String infoSeparator = "|";


    /**
     * 从Properties加载配置
     */
    public void loadFromProperties(Properties properties) {
        if (properties.containsKey("enabled")) {
            this.enabled = Boolean.parseBoolean(properties.getProperty("enabled"));
        }
        if (properties.containsKey("markSelect")) {
            this.markSelect = Boolean.parseBoolean(properties.getProperty("markSelect"));
        }
        if (properties.containsKey("markInsert")) {
            this.markInsert = Boolean.parseBoolean(properties.getProperty("markInsert"));
        }
        if (properties.containsKey("markUpdate")) {
            this.markUpdate = Boolean.parseBoolean(properties.getProperty("markUpdate"));
        }
        if (properties.containsKey("markDelete")) {
            this.markDelete = Boolean.parseBoolean(properties.getProperty("markDelete"));
        }
        if (properties.containsKey("debugEnabled")) {
            this.debugEnabled = Boolean.parseBoolean(properties.getProperty("debugEnabled"));
        }
        if (properties.containsKey("verboseLogging")) {
            this.verboseLogging = Boolean.parseBoolean(properties.getProperty("verboseLogging"));
        }
        if (properties.containsKey("markPrefix")) {
            this.markPrefix = properties.getProperty("markPrefix");
        }
        if (properties.containsKey("markSuffix")) {
            this.markSuffix = properties.getProperty("markSuffix");
        }
        if (properties.containsKey("includeFullInfo")) {
            this.includeFullInfo = Boolean.parseBoolean(properties.getProperty("includeFullInfo"));
        }
        if (properties.containsKey("includeTimestamp")) {
            this.includeTimestamp = Boolean.parseBoolean(properties.getProperty("includeTimestamp"));
        }
        if (properties.containsKey("includeThreadInfo")) {
            this.includeThreadInfo = Boolean.parseBoolean(properties.getProperty("includeThreadInfo"));
        }
        if (properties.containsKey("includeCustomInfo")) {
            this.includeCustomInfo = Boolean.parseBoolean(properties.getProperty("includeCustomInfo"));
        }
        if (properties.containsKey("maxCustomInfoLength")) {
            this.maxCustomInfoLength = Integer.parseInt(properties.getProperty("maxCustomInfoLength"));
        }
        if (properties.containsKey("infoSeparator")) {
            this.infoSeparator = properties.getProperty("infoSeparator");
        }
    }

    /**
     * 验证配置的有效性
     */
    public boolean isValid() {
        return markSuffix != null && markSuffix != null &&
                infoSeparator != null && maxCustomInfoLength > 0;
    }

    /**
     * 获取配置摘要信息
     */
    public String getConfigSummary() {
        return String.format("SqlMarkingConfig[enabled=%s, select=%s, insert=%s, update=%s, delete=%s, debug=%s]",
                enabled, markSelect, markInsert, markUpdate, markDelete, debugEnabled);
    }
}