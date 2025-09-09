package org.aione.plugin.sqlmarking;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * 染色SQL源实现，继承自MyBatis的StaticSqlSource
 * 用于创建带有数据源染色标记的BoundSql对象
 * 主要功能是在SQL执行时标记其使用的数据源类型（读/写）
 * 
 * @author Billy
 * @version 1.0 
 * @since 2025-05-13
 */
public class SqlMarkedSource extends org.apache.ibatis.builder.StaticSqlSource {

    private final String sql;
    private final List<ParameterMapping> parameterMappings;
    private final Configuration configuration;

    public SqlMarkedSource(Configuration configuration, String sql) {
        super(configuration, sql);
        this.configuration = configuration;
        this.sql = sql;
        this.parameterMappings = null;
    }

    public SqlMarkedSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        super(configuration, sql, parameterMappings);
        this.configuration = configuration;
        this.sql = sql;
        this.parameterMappings = parameterMappings;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = new BoundSql(configuration, sql, parameterMappings, parameterObject);
        return boundSql;
    }
}