package org.aione.plugin.sqlmarking;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MyBatis SQL染色拦截器
 * 
 * 功能特性：
 * 1. 轻量高效，对业务代码无侵入
 * 2. 支持SELECT、INSERT、UPDATE、DELETE等所有SQL语句
 * 3. 支持无WHERE条件SQL的标记增强
 * 4. 不改变SQL指纹，保持原有执行计划
 * 5. 内置statementId、PFinderId，方便分布式跟踪和定位
 * 6. 提供附加信息传递入口，支持自定义染色信息
 *
 * @author Billy
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlMarkingInterceptor implements Interceptor {

    /**
     * SQL染色配置
     * -- GETTER --
     *  获取当前配置
     * -- SETTER --
     *  设置染色配置


     */
    @Setter
    @Getter
    private SqlMarkingConfig config;
    
    /**
     * 分布式追踪标识生成器
     */
    private final SqlMarkingIdGenerator idGenerator;
    
    /**
     * SQL染色处理器
     */
    private final SqlMarkingProcessor processor;
    
    /**
     * 执行计数器
     */
    private final AtomicLong executionCounter = new AtomicLong(0);

    public SqlMarkingInterceptor() {
        this.config = new SqlMarkingConfig();
        this.idGenerator = new SqlMarkingIdGenerator();
        this.processor = new SqlMarkingProcessor();
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 检查染色功能是否启用
        if (!config.isEnabled()) {
            return invocation.proceed();
        }

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];

        // 获取原始SQL
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        // 检查是否需要标记此SQL类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        if (!shouldMarkSql(sqlCommandType)) {
            return invocation.proceed();
        }

        // 检查SQL是否已经被标记，避免重复标记
        if (isAlreadyMarked(originalSql)) {
            return invocation.proceed();
        }

        try {
            // 生成标记信息
            SqlMarkingInfo markingInfo = createMarkingInfo(mappedStatement, sqlCommandType);

            // 执行SQL标记
            String markedSql = processor.markSql(originalSql, markingInfo);

            // 记录标记信息（用于调试和监控）
            if (config.isDebugEnabled()) {
                logMarkingInfo(mappedStatement.getId(), originalSql, markedSql, markingInfo);
            }

            // 创建新的BoundSql对象，包含标记后的SQL
            BoundSql newBoundSql = new BoundSql(
                mappedStatement.getConfiguration(),
                markedSql,
                boundSql.getParameterMappings(),
                parameter
            );

            // 复制原有的附加参数
//            copyAdditionalParameters(boundSql, newBoundSql);

            // 创建新的MappedStatement
            MappedStatement newMappedStatement = copyMappedStatement(mappedStatement, newBoundSql);
            args[0] = newMappedStatement;

            // 执行标记后的SQL
            Object proceed = invocation.proceed();
            return proceed;

        } catch (Exception e) {
            log.error("SQL标记处理异常，使用原始SQL执行 statementId: {}, error: {}",
                mappedStatement.getId(), e.getMessage(), e);
            // 异常情况下使用原始SQL执行，确保业务不受影响
            return invocation.proceed();
        }
    }

    /**
     * 判断是否需要对此SQL类型进行标记
     */
    private boolean shouldMarkSql(SqlCommandType sqlCommandType) {
        switch (sqlCommandType) {
            case SELECT:
                return config.isMarkSelect();
            case INSERT:
                return config.isMarkInsert();
            case UPDATE:
                return config.isMarkUpdate();
            case DELETE:
                return config.isMarkDelete();
            default:
                return false;
        }
    }

    /**
     * 创建SQL标记信息
     */
    private SqlMarkingInfo createMarkingInfo(MappedStatement mappedStatement, SqlCommandType sqlCommandType) {
        SqlMarkingInfo markingInfo = new SqlMarkingInfo();
        
        // 设置基础信息
        markingInfo.setStatementId(mappedStatement.getId());
        markingInfo.setSqlCommandType(sqlCommandType);
        markingInfo.setExecutionId(executionCounter.incrementAndGet());

        // 生成分布式追踪标识
        markingInfo.setPFinderId(idGenerator.generatePFinderId());
        markingInfo.setTraceId(idGenerator.generateTraceId());
        
        // 设置时间戳
        markingInfo.setTimestamp(System.currentTimeMillis());
        
        // 获取自定义标记信息
        SqlMarkingContext context = SqlMarkingContext.getCurrentContext();
        if (context != null) {
            markingInfo.setCustomInfo(context.getCustomInfo());
            markingInfo.setThreadId(context.getThreadId());
            markingInfo.setUserId(context.getUserId());
        } else {
            // 默认设置当前线程ID
            markingInfo.setThreadId(Thread.currentThread().getId());
        }
        
        return markingInfo;
    }

    /**
     * 复制MappedStatement，使用新的BoundSql
     */
    private MappedStatement copyMappedStatement(MappedStatement original, BoundSql newBoundSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
            original.getConfiguration(),
            original.getId(),
            new SqlMarkedSource(original.getConfiguration(), newBoundSql.getSql(), newBoundSql.getParameterMappings()),
            original.getSqlCommandType()
        );
        
        builder.resource(original.getResource());
        builder.fetchSize(original.getFetchSize());
        builder.timeout(original.getTimeout());
        builder.statementType(original.getStatementType());
        builder.keyGenerator(original.getKeyGenerator());
        builder.keyProperty(original.getKeyProperties() != null ? String.join(",", original.getKeyProperties()) : null);
        builder.keyColumn(original.getKeyColumns() != null ? String.join(",", original.getKeyColumns()) : null);
        builder.databaseId(original.getDatabaseId());
        builder.lang(original.getLang());
        builder.resultOrdered(original.isResultOrdered());
        builder.resultSets(original.getResultSets() != null ? String.join(",", original.getResultSets()) : null);
        builder.resultMaps(original.getResultMaps());
        builder.resultSetType(original.getResultSetType());
        builder.flushCacheRequired(original.isFlushCacheRequired());
        builder.useCache(original.isUseCache());
        builder.cache(original.getCache());
        builder.parameterMap(original.getParameterMap());
        
        return builder.build();
    }

    /**
     * 记录标记信息日志
     */
    private void logMarkingInfo(String statementId, String originalSql, String markedSql, SqlMarkingInfo markingInfo) {
        log.debug("SQL标记执行 - StatementId: {}, PFinderId: {}, TraceId: {}, ExecutionId: {}, ThreadId: {}",
            statementId,
            markingInfo.getPFinderId(),
            markingInfo.getTraceId(),
            markingInfo.getExecutionId(),
            markingInfo.getThreadId()
        );
        
        if (config.isVerboseLogging()) {
            log.debug("原始SQL: {}", originalSql.replaceAll("\\s+", " ").trim());
            log.debug("标记SQL: {}", markedSql.replaceAll("\\s+", " ").trim());
        }
    }

    /**
     * 检查SQL是否已经被标记
     */
    private boolean isAlreadyMarked(String sql) {
        if (sql == null) {
            return false;
        }
        return sql.contains(config.getMarkPrefix()) && sql.contains(config.getMarkSuffix());
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 从配置文件中读取配置参数
        if (properties != null) {
            config.loadFromProperties(properties);
        }
    }

    /**
     * 获取执行统计信息
     */
    public long getExecutionCount() {
        return executionCounter.get();
    }

    /**
     * 重置执行计数器
     */
    public void resetExecutionCount() {
        executionCounter.set(0);
    }
}