package org.aione.sqlmarking;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SQL染色插件自动配置类
 * 使用BeanPostProcessor避免循环依赖问题
 *
 * @author Billy
 */
@Slf4j
@Configuration
@ConditionalOnClass({SqlSessionFactory.class})
@EnableConfigurationProperties(SqlMarkingConfig.class)
public class SqlMarkingAutoConfiguration {

    @Bean
    public SqlMarkingInterceptor sqlMarkingInterceptor(SqlMarkingConfig sqlMarkingConfig) {
        SqlMarkingInterceptor interceptor = new SqlMarkingInterceptor();
        interceptor.setConfig(sqlMarkingConfig);
        log.info("SQL染色拦截器创建完成: {}", sqlMarkingConfig.getConfigSummary());
        return interceptor;
    }

    @Bean
    public BeanPostProcessor sqlMarkingBeanPostProcessor(SqlMarkingInterceptor sqlMarkingInterceptor) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof SqlSessionFactory) {
                    if (sqlMarkingInterceptor.getConfig().isEnabled()) {
                        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
                        sqlSessionFactory.getConfiguration().addInterceptor(sqlMarkingInterceptor);
                        log.info("SQL染色拦截器已添加到SqlSessionFactory: {} ({})",
                                sqlSessionFactory.getClass().getSimpleName(), beanName);
                    }
                }
                return bean;
            }
        };
    }
}